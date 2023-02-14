/*
 * Copyright © 2017-2019 MarkLogic Corporation
 */

'use strict';

const op = require('/MarkLogic/optic');
const geojson = require('/MarkLogic/geospatial/geojson.xqy');
const sm = require('/ext/serviceModel.sjs');
const sql2optic = require('/ext/sql/sql2optic.sjs');
const geostats = require('/ext/geo/geostats.js');
const geoextractor = require('/ext/geo/extractor.sjs');
const qd = require('/ext/query/ctsQueryDeserialize.sjs').qd;
const gsu = require('/ext/search/geo-search-util.xqy');

const MAX_RECORD_COUNT = 5000;

const joinFunctionMap = {
  "inner":"joinInner",
  "left outer":"joinLeftOuter",
  "full outer":"joinFullOuter"
};


function post(context, params, input) {
  xdmp.trace("GDS-DEBUG", "Starting post");
  // assume the input is the request that was sent to the koop provider getData() function

  try {
    const geoJson =  getData(fn.head(xdmp.fromJSON(input)));
    xdmp.trace("GDS-DEBUG", JSON.stringify(geoJson));
    return {
      "$version": require('/ext/version.sjs').version,
      "$timestamp": new Date().toISOString(),
      ...geoJson
    };
  } catch (err) {
    if (typeof err === "string") {
      if (err.endsWith("not found")) {
        returnErrToClient(404, "Not Found", err);
      } else {
        returnErrToClient(400, "Bad Request", err);
      }
    }
    returnErrToClient(500, 'Error handling request', err.toString());
  }
}

function returnErrToClient(statusCode, statusMsg, body) {
  xdmp.trace("GDS-DEBUG", "Starting returnErrToClient");
  fn.error(
    null,
    'RESTAPI-SRVEXERR',
    Sequence.from([statusCode, statusMsg, body])
  );
  // unreachable - control does not return from fn.error.
};

// the same as the koop provider function without the callback parameter
xdmp.trace("GDS-DEBUG", "Starting without");
function getData(req) {
  xdmp.trace("GDS-DEBUG", "Starting getData");
  xdmp.trace("GDS-REQUEST", JSON.stringify(req));

  if (req.geoserver) {
    return getGeoServerData(req);
  }
  else if (req.params.method == "query") {
    xdmp.trace("GDS-DEBUG", "Method 'query'");
    return query(req);
  } else if (req.params.method == "exportPlan") {
    xdmp.trace("GDS-DEBUG", "Method 'exportPlan'");
    return query(req, true);
  } else if (req.params.method == "generateRenderer") {
    xdmp.trace("GDS-DEBUG", "Method 'generateRenderer'");
    return queryClassificationValues(req);
  } else {
    xdmp.trace("GDS-DEBUG", "Method not found, just get the descriptors");
    if (req.params.layer >= 0) {
      return sm.generateLayerDescriptor(req.params.id, req.params.layer);
    } else {
      return sm.generateServiceDescriptor(req.params.id);
    }
  }

  // return an unsupported error
  returnErrToClient(501, 'Request parameters not supported', xdmp.quote(req));
}

function getGeoServerData(req) {
  if (req.geoserver.method == "getLayerNames") {
    return getGeoServerLayerNames();
  }
  else if (req.geoserver.method == "getLayerSchema") {
    return sm.getGeoServerLayerSchema(req.geoserver.layerName);
  }
}

function getGeoServerLayerNames() {
  const collection = "http://marklogic.com/feature-services";
  let layerNames = [];
  for (let descriptorDoc of cts.search(
    cts.andQuery([
      cts.collectionQuery(collection),
      cts.jsonPropertyScopeQuery("geoServerLayerName", cts.trueQuery())
    ])
    )) {
    let descriptor = descriptorDoc.toObject();

    for (let layer of descriptor.layers) {
      if (layer.geoServerMetadata && layer.geoServerMetadata.geoServerLayerName)
        layerNames.push(layer.geoServerMetadata.geoServerLayerName);
    }
  }
  return layerNames;
}

function getDateTime(durationOrTimestamp) {
  return fn.head(
    xdmp.xqueryEval(`
      declare variable $d external;
      if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dayTimeDuration", $d)) then
        fn:current-dateTime() + xs:dayTimeDuration($d)
      else if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "yearMonthDuration", $d)) then
        fn:current-dateTime() + xs:yearMonthDuration($d)
      else if (xdmp:castable-as("http://www.w3.org/2001/XMLSchema", "dateTime", $d)) then
        xs:dateTime($d)
      else
        ()
    `,
    {"d": durationOrTimestamp})
  );
}

function query(req, exportPlan=false) {
  xdmp.trace("GDS-DEBUG", "Starting query");
  // always return a FeatureCollection for now
  const geojson = {
    type : 'FeatureCollection',
    metadata : {
      name: req.params.id,
      maxRecordCount: MAX_RECORD_COUNT
    },
    filtersApplied: {
      geometry: true, // true if a geometric filter has already been applied to the data
      where: true, // true if a sql-like where filter has already been applied to the data
      offset: true,
      limit : true
    }
  };

  if (exportPlan) {
    xdmp.trace("GDS-DEBUG", "exportPlan: running getObjects...");
    return getObjects(req, true);
  } else if (req.query.returnCountOnly) {
    xdmp.trace("GDS-DEBUG", "getting count");

    req.query.outStatistics = [
      { outStatisticFieldName : "count", statisticType : "count" }
    ];

    geojson.count = Array.from(aggregate(req))[0].count;

  } else if (req.query.outStatistics != null) {

    xdmp.trace("GDS-DEBUG", "running aggregation");
    geojson.statistics = Array.from(aggregate(req));

  } else  {

    xdmp.trace("GDS-DEBUG", "getting objects for geojson.features");
    const objects = getObjects(req);
    xdmp.trace("GDS-DEBUG", "Object Results found:");
    xdmp.trace("GDS-DEBUG", objects);
    geojson.features = objects.result;
    if (geojson.features.geometry) {
      const geometryType = typeof geojson.features.geometry;
      switch(geometryType) {
        case "object":
            xdmp.trace("GDS-DEBUG", "geojson.features.geometry is an object");
            break;
        case "string":
            xdmp.trace("GDS-DEBUG", "geojson.features.geometry is an string");
            break;
        default:
            xdmp.trace("GDS-DEBUG", "geojson.features.geometry is:" + typeof geojson.features.geometry);
      }
    }

    xdmp.trace("GDS-DEBUG", "limitExceeded flag :" + objects.limitExceeded);

    // we should only get this once in the process but do this for now to test
    const serviceId = req.params.id;
    //const layerModel = sm.generateLayerDescriptor(serviceId, req.params.layer);
    const layerModel = sm.getLayerModel(serviceId, req.params.layer);
    const layerFields = sm.getColumnDefs(serviceId, req.params.layer);

    // set the field metadata in the response
    // but only set it for fields we are returning

    // we already called parseOutfields to get the list of fields
    // maybe we should move all the parse steps into one place and
    // create a parsed request object we can use throughout?
    const outFields = {};
    parseOutFields(req.query).map(f => { outFields[f] = true; });

    if (Object.keys(outFields).length === 0 || outFields["*"]) {
      geojson.metadata.fields = layerFields;
    } else {
      geojson.metadata.fields = layerFields.filter(f => {
        return outFields[f.name];
      });
    }

    geojson.metadata.limitExceeded = objects.limitExceeded;

    geojson.metadata.idField = layerModel.idField;
    geojson.metadata.displayField = layerModel.displayField;
  }

  // GeoJSON feature collections must always have a "features" object array, even if empty.
  // See GeoJSON RFC: https://tools.ietf.org/html/rfc7946#section-3.2
  if (!geojson.hasOwnProperty("features")) {
    geojson.features = [];
  }

  return geojson;
}

/**
 * This is used for generateRenderer requests. Generates an aggregation query to return
 * all the unique values in a field.
 * @param {Object} req - The request from Koop
 */
function queryClassificationValues(req) {
  xdmp.trace("GDS-DEBUG", "Starting queryClassificationValues");

  const def = parseClassificationDef(req.query);

  const q = {
    groupByFieldsForStatistics : null,
    outStatistics : [{
      statisticType : "count",
      onStatisticField : null,
      outStatisticFieldName : "count"
    }],
    where : req.query.where
  };

  switch (def.type) {
    case "classBreaksDef":
      q.groupByFieldsForStatistics = def.classificationField;
      break;
    case "uniqueValueDef":
      if (typeof def.uniqueValueFields === "string") {
        q.groupByFieldsForStatistics = def.uniqueValueFields.split(def.fieldDelimiter).join(",");
      } else {
        q.groupByFieldsForStatistics = def.uniqueValueFields.join(",");
      }

      break;
    default:
      throw "Unsupported classificationDef.type: " + def.type;
  }

  const result = query({ params : req.params, query : q });
  xdmp.trace("GDS-DEBUG", "queryClassificationValues calculating breaks for " + result.statistics.length + " values");

  const classStatistics = {
    geometryType : sm.getLayerModel(req.params.id, req.params.layer).geometryType
  }

  //http://pro.arcgis.com/en/pro-app/help/mapping/symbols-and-styles/data-classification-methods.htm

  if (def.type === "classBreaksDef") {
    const values = result.statistics.map((stat) => {
      return stat[def.classificationField];
    });

    // it looks like the breaks need to be in pairs of min and max values
    let classValues = null;

    switch (def.classificationMethod) {
      case "esriClassifyNaturalBreaks":
        classValues = (new geostats(values)).getClassJenks(def.breakCount);
        break;
      case "esriClassifyEqualInterval":
        classValues = (new geostats(values)).getClassEqInterval(def.breakCount);
        break;
      case "esriClassifyQuantile":
        classValues = (new geostats(values)).getClassQuantile(def.breakCount);
        break;
      case "esriClassifyStandardDeviation":
        classValues = (new geostats(values)).getClassStdDeviation(def.standardDeviationInterval);
        break;
      case "esriClassifyGeometricalInterval":
        classValues = (new geostats(values)).getClassGeometricProgression(def.breakCount);
        break;
      default:
        throw "Unsupported classificationMethod: " + def.classificationMethod;
    }

    // make the values into range pairs
    classStatistics.classBreaks = valuesToRanges(classValues);
  } else {
    classStatistics.uniqueValues = result.statistics;
  }

  // override the result statistics with the new statistics object
  result.statistics = classStatistics;

  return result;
}

function valuesToRanges(values) {
  xdmp.trace("GDS-DEBUG", "Starting valuesToRanges");
  const ranges = Array(values.length - 1);
  for (let i = 0; i < ranges.length; i++) {
    ranges[i] = [values[i], values[i + 1]];
  }
  return ranges;
}


function parseWhere(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseWhere");
  // Any legal SQL where clause operating on the fields in the layer is allowed
  // Example: where=POP2000 > 350000

  const where = query.where;
  let whereQuery = null;
  if (where && typeof where !== 'string' && where.search) {
    // where.search contains Combined Query
    whereQuery = gsu.parseCombined(where, query.optionsName);

  } else
  if (!where || where === "1=1" || where === "1 = 1" || where === "") {
    //whereQuery = cts.trueQuery();
    whereQuery = op.eq(1, 1)

  } else {
    whereQuery = sql2optic.where(where);
  }

  xdmp.trace("GDS-DEBUG", "where: " + whereQuery);

  return whereQuery;
}

function parseGeometry(query, layerModel) {
  xdmp.trace("GDS-DEBUG", "Starting parseGeometry");

  // the koop provider code will convert the Esri geometry objects into GeoJSON
  // in WGS84 and place it in the query.extension.geometry property
  let geoQuery = null;
  if (query.extension && query.extension.geometry) {

    let regions = null;
    if (!query.geometryType || query.geometryType.toLowerCase() === "esrigeometryenvelope") {
      // handle this because the koop server changes boxes to GeoJSON polygons but
      // a box is better for this
      regions = convertEnvelopePolygon(query);
    } else {
      // convert GeoJSON into cts regions
      regions = geojson.parseGeojson(adjustEsriPolygon(query.extension.geometry));
    }

    const pointQuery = geoextractor.getPointQuery(regions, layerModel);
    const operation = parseRegionOperation(query);
    const regionQuery = geoextractor.getRegionQuery(regions, operation, layerModel);

    let queries = [];
    if (pointQuery) { queries.push(pointQuery); }
    if (regionQuery) { queries.push(regionQuery); }
    if (queries.length > 0) {
      geoQuery = cts.orQuery(queries);
    }
  }

  if (!geoQuery) {
    // just match everything
    geoQuery = cts.trueQuery();
  }

  xdmp.trace("GDS-DEBUG", "geometry: " + geoQuery);

  return geoQuery;
}

function clampPointArray(coord) {
  // clamps [lon, lat] array to maximum values
  if (coord[1] >= 90.0)       coord[1] = 90;
  else if (coord[1] <= -90.0) coord[1] = -90;

  if (coord[0] >= 180.0)       coord[0] = 180;
  else if (coord[0] <= -180.0) coord[0] = -180;
}

function adjustEsriBox(esriBox) {
  if (esriBox.hasOwnProperty("spatialReference") && esriBox.spatialReference.wkid == 4326) {
    if (esriBox.ymin <= -90)  esriBox.ymin =  -89.999;
    if (esriBox.xmin <= -180) esriBox.xmin = -179.999;
    if (esriBox.ymax >= 90)   esriBox.ymax =   89.999;
    if (esriBox.xmax >= 180)  esriBox.xmax =  179.999;
  }
  return esriBox;
}

function adjustCoordinatesArrayR(coords) {
  if (coords.length === 2 && coords[0].constructor == Number && coords[1].constructor == Number) {
      clampPointArray(coords);
  } else if (Array.isArray(coords[0])) { //assume it's an array of arrays, we won't get a mix
    coords.forEach(arr => {
        adjustCoordinatesArrayR(arr);
    });
  }
}

function adjustEsriPolygon(esriPolygon) {
  if (esriPolygon.hasOwnProperty("spatialReference") && esriPolygon.spatialReference.wkid == 4326) {
    adjustCoordinatesArrayR(esriPolygon.coordinates);
  }
  return esriPolygon;
}

function convertEnvelopePolygon(query) {
  xdmp.trace("GDS-DEBUG", "Starting convertEnvelopePolygon");
  // the koop server converts Esri envelopes to GeoJSON polygons

  // convert them to boxes for more efficient seach
  // TODO: file an issue about the winding order (they do not follow the right hand rule)
  //{
  //  type: 'Polygon',
  //  coordinates: [[
  //    [geom.xmin, geom.ymin],
  //    [geom.xmin, geom.ymax],
  //    [geom.xmax, geom.ymax],
  //    [geom.xmax, geom.ymin],
  //    [geom.xmin, geom.ymin]
  //  ]]
  //}

  const geometry = adjustEsriBox(query.extension.geometry);
  const coords = adjustEsriPolygon(geometry).coordinates[0];
  const south = coords[0][1];
  const west = coords[0][0];
  const north = coords[2][1];
  const east = coords[2][0];
  const box = { south : south, west : west, north : north, east : east }

  return splitBox(box).toArray();
}

function splitCtsBox(box) {
  xdmp.trace("GDS-DEBUG", "Starting splitCtsBox");
  return splitBox({ south : cts.boxSouth(box), west : cts.boxWest(box), north : cts.boxNorth(box), east : cts.boxEast(box) })
}

function splitBox(box) {
  xdmp.trace("GDS-DEBUG", "Starting splitBox");
  if (Math.round(Math.abs(box.west - box.east)) >= 179) {
    // check east/west
    const middle = (box.west + box.east) / 2.0;
    return Sequence.from([
      { south : box.south, west : box.west, north : box.north, east : middle },
      { south : box.south, west : middle, north : box.north, east : box.east }
    ].map(splitBox))
  } else if (Math.round(Math.abs(box.south - box.north)) >= 179) {
    // check north/south
    const middle = (box.south + box.north) / 2.0;
    return Sequence.from([
      { south : box.south, west : box.west, north : middle, east : box.east },
      { south : middle, west : box.west, north : box.north, east : box.east }
    ].map(splitBox))
  }
  return Sequence.from([ cts.box(box.south, box.west, box.north, box.east) ]);
}

function parseRegionOperation(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseRegionOperation");
  // cts region operators: contains, covered-by, covers, disjoint, intersects, overlaps, within
  // default to intersects

  // TODO: verify mapping of Esri spatial relations to MarkLogic operations
  // can we implement the other Esri relations with combinations of the MarkLogic
  // operations?

if (query.spatialRel) {
    switch(query.spatialRel.toLowerCase()) {
      case "esrispatialrelintersects":
        return "intersects";
      case "esrispatialrelcontains":
        return "contains";
      case "esrispatialrelcrosses":
        return "crosses";
      case "esrispatialrelwithin":
        return "within";
      case "esrispatialreloverlaps":
        return "overlaps";
      case "esrispatialreltouches":
        return "touches";
      case "esrispatialrelenvelopeintersects":
      case "esrispatialrelindexintersects":
      case "esrispatialrelrelation":
      default:
        throw "Unsupported geospatial operation: " + query.spatialRel;
    }
  } else {
    return "intersects";
  }
}

function parseOutStatistics(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseOutStatistics");
  // outStatistics may be a JSON string of an array of stats definitions
  // or it may be converted already
  // see http://resources.arcgis.com/en/help/rest/apiref/fsquery.html

  return (typeof query.outStatistics === "string") ?
    JSON.parse(query.outStatistics) : query.outStatistics;
}

function parseClassificationDef(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseClassificationDef");
  return (typeof query.classificationDef === "string") ?
    JSON.parse(query.classificationDef) : query.classificationDef;
}


function parseOrderByFields(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseOrderByFields");
  // orderByFields is supported on only those layers / tables that indicate supportsAdvancedQueries is true.
  // orderByFields defaults to ASC (ascending order) if <ORDER> is unspecified.

  // Syntax: orderByFields=field1 <ORDER>, field2 <ORDER>, field3 <ORDER>
  // Example: orderByFields=STATE_NAME ASC, RACE DESC, GENDER

  // return an array of { field, order } objects
  let fields = [];
  if (query.orderByFields) {
    fn.tokenize(fn.normalizeSpace(query.orderByFields), ", ?").toArray().map((field) => {
     const parts = fn.tokenize(field, " +").toArray();
     fields.push(
       { "field" : parts[0], "order" : (parts[1] || "ASC" ) }
     );
    });
  }

  return fields;
}

function parseObjectIds(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseObjectIds");
  //Syntax: objectIds=<objectId1>, <objectId2>
  //Example: objectIds=37, 462
  let ids = null;
  if (query.objectIds) {
    if (typeof query.objectIds === "string") {
      ids = fn.tokenize(query.objectIds, ", ?").toArray();
    } else {
      ids = [ query.objectIds ];
    }
  }
  return ids;
}

function parseOutFields(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseOutFields");
  // Description: The list of fields to be included in the returned resultset. This list is a comma delimited list of field names.
  // You can also specify the wildcard "*" as the value of this parameter. In this case, the query results include all the field values.
  // Note that the wildcard also implicitly implies returnGeometry=true and setting returnGeometry to false will have no effect.

  // Example: outFields=AREANAME,ST,POP2000
  // Example (wildcard usage): outFields=*
  let fields = [];
  if (query.outFields) {
    fields = fn.tokenize(query.outFields, ", ?").toArray();
  }

  return fields;
}

function parseGroupByFields(query) {
  xdmp.trace("GDS-DEBUG", "Starting parseGroupByFields");
  // Description: One or more field names on which the values need to be grouped for calculating the statistics.
  // Note: groupByFieldsForStatistics is valid only when outStatistics parameter is used.
  // Syntax: groupByFieldsForStatistics=field1, field2
  // Example: groupByFieldsForStatistics=STATE_NAME, GENDER

  let fields = [];
  if (query.groupByFieldsForStatistics) {
    fields = fn.tokenize(query.groupByFieldsForStatistics, ", ?").toArray();
  }

  return fields;
}

/**
 * Builds an optic where clause for the time bounds given
 * the the layer model and req.
 *
 * Assume dates in ML documents are in UTC for now.
 * Currently only handling startDate bounds.
 *
 * TODO implement bounds for start AND end time
 * TODO get time zone info from layerModel.timeInfo.timeReference.timeZone
 *  and convert time to TZ setting, https://developers.arcgis.com/javascript/3/jsapi/timeinfo.html
 *
 * @param {object} layerModel - the layer description json data
 * @param {object} req - the request parameters passed into the service (json)
 * @return {booleanExpression} result of optic expressions, input to op.where
*/
function getTimeBoundingWhereQuery(layerModel, req) {
  xdmp.trace("GDS-DEBUG", "Starting getTimeBoundingWhereQuery");
  let startTimeField = layerModel.timeInfo.startTimeField;
  let endTimeField = layerModel.timeInfo.endTimeField;

  if(req.query.time.toString().indexOf(",") >= 0) {  //Handle Time range
    xdmp.trace("GDS-DEBUG", "Handle Time Range");

    // "null" can be passed in as a parameter to the time array.
    let timeRange = req.query.time.split(",");
    let startTime = (timeRange[0] && timeRange[0].trim() != "null") ? new Date(parseInt(timeRange[0].trim())) : new Date("0001-01-01T00:00:00");
    let endTime = (timeRange[1] && timeRange[1].trim() != "null") ? new Date(parseInt(timeRange[1].trim())) : new Date("9999-12-31T00:00:00");
    let utcStartTime = new Date(Date.UTC(startTime.getFullYear(), startTime.getMonth(), startTime.getDate()));
    let utcEndTime = new Date(Date.UTC(endTime.getFullYear(), endTime.getMonth(), endTime.getDate()));

    xdmp.trace("GDS-DEBUG", "Time Range " + utcStartTime.toISOString() + " - " + utcEndTime.toISOString());
    return op.and(
      op.ge(op.col(startTimeField), utcStartTime.toISOString()),
      op.lt(op.col(startTimeField), utcEndTime.toISOString())
    );
  } else {  //Handle time instance
    xdmp.trace("GDS-DEBUG", "Handle Time Instance");
    let startTime = new Date(parseInt(req.query.time.trim()));
    let utcStartTime = new Date(Date.UTC(startTime.getFullYear(), startTime.getMonth(), startTime.getDate()));

    xdmp.trace("GDS-DEBUG", "Time Instance " + utcStartTime.toISOString());

    return op.eq(op.col(startTimeField), utcStartTime.toISOString());
  }
}

/**
 *
 * @param requestQuery the query from the caller's request
 * @param layerModel
 * @returns a CTS query
 */
function buildBoundingQuery(requestQuery, layerModel) {
  const boundingQueries = [ parseGeometry(requestQuery, layerModel) ];

  if (layerModel.boundingQuery) {
    boundingQueries.push(qd.query(layerModel.boundingQuery));
  }
  if (layerModel.temporalBounds) {
    boundingQueries.push(getTemporalQuery(layerModel.temporalBounds));
  }

  // alwaysIncludeQuery is an optional feature for a layer definition to ensure that certain data is always included,
  // which implies usage of an "or" query. See https://github.com/marklogic-community/marklogic-geo-data-services/issues/76
  // for the original requirements.
  const boundingQuery = layerModel.alwaysIncludeQuery ?
    cts.orQuery([qd.query(layerModel.alwaysIncludeQuery), cts.andQuery(boundingQueries)]) :
    cts.andQuery(boundingQueries);

  xdmp.trace("GDS-DEBUG", "boundingQuery: " + xdmp.toJsonString(boundingQuery));
  return boundingQuery;
}

/**
 * Create a unique column name for the doc id join
 * This is used to build the name of the column containing the docId for joins.
 * This must be unique with respect to the column names of the view in the query,
 * but it must also be repeatable so that query caches are used appropriately in the Optic engine.
 * @param serviceName - The name of the feature service being queried
 * @returns
 */
function getDefaultDocIdColumn(serviceName) {
  return "docId_" + serviceName + "_";
}

// returns a Sequence of documents
function getObjects(req, exportPlan=false) {

  xdmp.trace("GDS-DEBUG", "Starting getObjects");
  xdmp.trace("GDS-DEBUG", "getLayerModel(" + req.params.id + ", " + req.params.layer + ")" );
  const layerModel = sm.getLayerModel(req.params.id, req.params.layer);

  const requestQuery = req.query;
  const orderByFields = parseOrderByFields(requestQuery);
  const defaultDocId = getDefaultDocIdColumn(req.params.id);

  let whereQuery = parseWhere(requestQuery);

  let outFields;
  if (requestQuery.returnIdsOnly) {
    if (layerModel.idField) {
      outFields = [layerModel.idField];
    } else {
      outFields = [ "OBJECTID" ];
    }
  } else {
    outFields = parseOutFields(requestQuery);
  }

  const returnGeometry = (requestQuery.returnGeometry || outFields[0] === "*") ? true : false;
  // Should this be geometry or geometrySource?  TJD--"geometry"
  // This should be geometry.source.  MDC
  const geometrySource = (layerModel && layerModel.geometry && layerModel.geometry.source) ? layerModel.geometry.source : null;
  xdmp.trace("GDS-DEBUG", "geometrySource = " + geometrySource);
  xdmp.trace("GDS-DEBUG", "returnGeometry = " + returnGeometry);


  whereQuery = updateWhereWithObjectIds(requestQuery, whereQuery, layerModel)

  // Initial Time bounding query implementation, GitHub Issue #13
  if(req.query.time && layerModel.timeInfo && layerModel.timeInfo.startTimeField) {
    whereQuery = op.and(whereQuery, getTimeBoundingWhereQuery(layerModel, req));
  }
  xdmp.trace("GDS-DEBUG", "whereQuery: " + whereQuery);

  const boundingQuery = buildBoundingQuery(requestQuery, layerModel);

  const offset = (!requestQuery.resultOffset ? 0 : Number(requestQuery.resultOffset));
  xdmp.trace("GDS-DEBUG", "offset: " + offset);

  // what if the number of ids passed in is more than the max?

  let limit = 0;
  if (requestQuery.resultRecordCount) {

    xdmp.trace("GDS-DEBUG", "Setting to limit to resultRecordCount");
    limit = Number(requestQuery.resultRecordCount)
  }
  else if ( requestQuery.returnIdsOnly ) {
    xdmp.trace("GDS-DEBUG", "Setting to limit to MAX_SAFE_INTEGER because we are only returning IDs");
    limit = Number.MAX_SAFE_INTEGER
  }
  else {
    xdmp.trace("GDS-DEBUG", "Setting to limit to MAX_RECORD_COUNT");
    limit = MAX_RECORD_COUNT
  }

  xdmp.trace("GDS-DEBUG", "limit: " + limit);
  const bindParams = {
    "offset" : offset,
    "limit" : ((limit != Number.MAX_SAFE_INTEGER) ? (limit+1) : Number.MAX_SAFE_INTEGER),
  };

  let pipeline;
  let columnDefs;
  if (layerModel.dataSources === undefined) {
    xdmp.trace("GDS-DEBUG", "layerModel.dataSources is undefined");
    const schema = layerModel.schema;
    const view = layerModel.view;
    columnDefs = sm.getColumnDefs(req.params.id, req.params.layer);

    xdmp.trace("GDS-DEBUG", "getObjects(): layerModel.dataSources === undefined, using " + defaultDocId + " as fragment id column");
    let viewPlan = op.fromView(schema, view, "", defaultDocId);
    xdmp.trace("GDS-DEBUG", "Pipeline[dataSources === undefined] Plan:");
    xdmp.trace("GDS-DEBUG", viewPlan);
    xdmp.trace("GDS-DEBUG", "Pipeline[dataSources === undefined] boundingQuery:");
    xdmp.trace("GDS-DEBUG", boundingQuery);
    xdmp.trace("GDS-DEBUG", "Pipeline[dataSources === undefined] layerModel:");
    xdmp.trace("GDS-DEBUG", layerModel);

    pipeline = initializePipeline(viewPlan, boundingQuery, layerModel);

    // joins?

  } else {
    const primaryDataSource = layerModel.dataSources[0];
    if (primaryDataSource.source === "view") {
      const schema = primaryDataSource.schema;
      const view = primaryDataSource.view;
      columnDefs = sm.getColumnDefs(req.params.id, req.params.layer);

      // Dynamically choosing prefix depending on existance of fragment ID column.
      const prefix = primaryDataSource.fragmentIdColumn ? null : "";
      const fragmentIdColumn = primaryDataSource.fragmentIdColumn ? primaryDataSource.fragmentIdColumn : defaultDocId;
      xdmp.trace("GDS-DEBUG", "fragmentIdColumn: " + fragmentIdColumn);
      let viewPlan = op.fromView(schema, view, prefix, fragmentIdColumn);

      xdmp.trace("GDS-DEBUG", viewPlan);
      xdmp.trace("GDS-DEBUG", "Pipeline[source === view] boundingQuery:");
      xdmp.trace("GDS-DEBUG", boundingQuery);
      xdmp.trace("GDS-DEBUG", "Pipeline[source === view] layerModel:");
      xdmp.trace("GDS-DEBUG", layerModel);

      xdmp.trace("GDS-DEBUG", "Pipeline[source === sparql] layerModel:");
      xdmp.trace("GDS-DEBUG", layerModel);
      pipeline = initializePipeline(viewPlan, boundingQuery, layerModel)
    }
    else if (primaryDataSource.source === "sparql") {
      columnDefs = sm.getColumnDefs(req.params.id, req.params.layer);
      let sparqlPlan = getPlanForDataSource(primaryDataSource);
      pipeline = initializePipeline(sparqlPlan, boundingQuery, layerModel);
    }
  }


  if (exportPlan) {
    pipeline = pipeline
      .where(whereQuery)
      .orderBy(getOrderByDef(orderByFields))
      .offset(bindParams.offset)
      .limit(bindParams.limit);
  }
  else {
    pipeline = pipeline
      .where(whereQuery)
      .orderBy(getOrderByDef(orderByFields))
      .offset(op.param("offset"))
      .limit(op.param("limit"));
  }

  // only join in the document if we need to get the geometry from the document
  if (returnGeometry) {
    xdmp.trace("GDS-DEBUG", "Returning Geometry");
    if (geometrySource && geometrySource.xpath && geometrySource.documentUriColumn) {
      xdmp.trace("GDS-DEBUG", "GeometrySource is xpath and documentUriColumn is specified");
      pipeline = pipeline.joinDoc(op.col('doc'), op.col(geometrySource.documentUriColumn))
    }
    else if (!geometrySource || geometrySource.xpath) {
      xdmp.trace("GDS-DEBUG", "GeometrySource is null or is XPath only");
      pipeline = pipeline.joinDoc(op.col('doc'), op.fragmentIdCol(defaultDocId))
    }
    //otherwise we must have column extraction
  }

  const extractor = geoextractor.getExtractor(layerModel);
  xdmp.trace("GDS-DEBUG", "extractor:");
  xdmp.trace("GDS-DEBUG", extractor);

  // TODO: see if there is any benefit to pushing the column select earlier in the pipeline
  // transform the rows into GeoJSON
  if (layerModel.idField) {
    xdmp.trace("GDS-DEBUG", "LayerID Field: " + layerModel.idField);
    pipeline = pipeline.select(getSelectDef(outFields, columnDefs, returnGeometry, extractor, exportPlan, layerModel.idField));
  } else {
    pipeline = pipeline.select(getSelectDef(outFields, columnDefs, returnGeometry, extractor, exportPlan));
  }

  if (exportPlan) {
    let exported = pipeline.export();
    xdmp.trace("GDS-DEBUG", "exported pipeline: ");
    xdmp.trace("GDS-DEBUG", exported);
    return exported;
  }
  else {
    // GeoJSON features must always have a "geometry" property; for cases where the feature has no
    // associated geometry data or "returnGeometry" is set to false, set "geometry" property to null.
    // See GeoJSON RFC: https://tools.ietf.org/html/rfc7946#section-3.2
    pipeline = pipeline.map((feature) => {
      var outFeature = feature;

      if (returnGeometry && extractor.hasExtractFunction()) {
        xdmp.trace("GDS-DEBUG", "Getting Extractor function");
        outFeature = extractor.extract(feature);
      }

      if (outFeature && !outFeature.hasOwnProperty("geometry")) {
        outFeature.geometry = null;
      }

      return outFeature;
    });

    xdmp.trace("GDS-DEBUG", "Now to pull results from the pipeline with the following bindParams");
    xdmp.trace("GDS-DEBUG", bindParams);
    const opticResult = Array.from(pipeline.result("object", bindParams));

    xdmp.trace("GDS-DEBUG", `Results pulled from Pipeline. Found ${opticResult.length} results.`);
    const opticResultCount = opticResult.length;



    if(opticResultCount >= (limit + 1) ){
      opticResult.pop();
      return {
        result : opticResult,
        limitExceeded : true
      }
    } else {
      return {
        result : opticResult,
        limitExceeded : false
      }
    }
  }
}


function getTemporalQuery(temporalReference) {
  if (temporalReference == null)
    return cts.trueQuery();

  let geQuery = null;
  let leQuery = null;
  if (temporalReference.referenceType == "field") {
    if (temporalReference.ge) {
      geQuery = cts.fieldRangeQuery(
        temporalReference.fieldName,
        ">=",
        getDateTime(temporalReference.ge));

    }
    if (temporalReference.le) {
      leQuery = cts.fieldRangeQuery(
        temporalReference.fieldName,
        "<=",
        getDateTime(temporalReference.le));
    }
  }

  if (geQuery && leQuery)
    return cts.andQuery([geQuery, leQuery]);
  else if (geQuery)
    return geQuery;
  else if (leQuery)
    return leQuery;
  else
    return cts.trueQuery();
};

function initializePipeline(viewPlan, boundingQuery, layerModel) {
  xdmp.trace("GDS-DEBUG", "Starting initializePipeline");
  let pipeline = viewPlan.where(boundingQuery);

  if (layerModel.dataSources && layerModel.dataSources.length > 1) {
    layerModel.dataSources.forEach((dataSource, index) => {
      if (index < 1) return;  // skip first element since it is the primary source
      pipeline = addJoinToPipeline(dataSource, viewPlan, pipeline);
    });
  } else {
    if (layerModel.joins && layerModel.joins.length > 0) {
      layerModel.joins.forEach((dataSource) => {
        pipeline = addJoinToPipeline(dataSource, viewPlan, pipeline);
      });
    }
  }
  return pipeline;
}

function getOpticJoinFunction(joinOn) {
  if (joinOn.joinType == null)
    return "joinInner";
  let joinFunc = joinFunctionMap[joinOn.joinType.toLowerCase()];
  if (joinFunc == null)
    returnErrToClient(500, joinFunc + " is not a supported joinType, check the layer descriptor->dataSource->joinOn.joinType");
  return joinFunc;
}

function addJoinToPipeline(dataSource, viewPlan, pipeline) {
  xdmp.trace("GDS-DEBUG", "Starting addJoinToPipeline");
  const dataSourcePlan = getPlanForDataSource(dataSource);
  const joinOn = dataSource.joinOn;
  let joinFunc = getOpticJoinFunction(joinOn);

  pipeline = pipeline[joinFunc](
    dataSourcePlan, op.on(op.col(joinOn.left), op.col(joinOn.right))
  )
  return pipeline;
}

function getPlanForDataSource(dataSource) {
  xdmp.trace("GDS-DEBUG", "Starting getPlanForDataSource");
  xdmp.trace("GDS-DEBUG", "Data source: " + JSON.stringify(dataSource));

  if (dataSource.source === "sparql") {
    let plan =  op.fromSPARQL(dataSource.query);
    if (dataSource.boundingQuery) {
      plan = plan.where(qd.query(dataSource.boundingQuery));
    }
    return plan;
  } else if (dataSource.source === "view") {
    if (dataSource.fragmentIdColumn) {
      xdmp.trace("GDS-DEBUG", "fragmentIdColumn: " + dataSource.fragmentIdColumn);
      return op.fromView(dataSource.schema, dataSource.view, null, dataSource.fragmentIdColumn)
    } else {
      xdmp.trace("GDS-DEBUG", "No Fragment ID Defined");
      return op.fromView(dataSource.schema, dataSource.view, "");
    }
  } else {
    returnErrToClient(500, 'Error handling request', "dataSource objects must specify a valid source ('view' or 'sparql')");
  }
}

function updateWhereWithObjectIds(query, whereQuery, layerModel) {
  const ids = parseObjectIds(query);
  if (ids) {
    // this assumes we are querying against the OBJECTID field as a number
    // should use a range index if we have one
    //const idsQuery = cts.jsonPropertyValueQuery("OBJECTID", ids.map(Number));
    xdmp.trace("GDS-DEBUG", "getting ids: " + ids);

    let idFieldName = "OBJECTID";
    if (layerModel.idField) {
      idFieldName = layerModel.idField;
    }
    const idExp = ids.map(value => op.eq(op.col(idFieldName), value));
    const idsQuery = (idExp.length === 1) ? idExp[0] : op.or(...idExp);

    whereQuery = op.and(whereQuery, idsQuery);
  }
  return whereQuery;
}

// returns a Sequence of aggregated results
function aggregate(req) {
  xdmp.trace("GDS-DEBUG", "Starting aggregate");
  // When using outStatistics the only other parameters that will be used are
  // groupByFieldsForStatistics, orderByFields, time, and where.

  // this will be the koop provider "id"
  const layerModel = sm.getLayerModel(req.params.id, req.params.layer);

  const requestQuery = req.query;
  const stats = parseOutStatistics(requestQuery)
  const groupByFields = parseGroupByFields(requestQuery);
  const orderByFields = parseOrderByFields(requestQuery);
  const defaultDocId = getDefaultDocIdColumn(req.params.id);

  const boundingQuery = buildBoundingQuery(requestQuery, layerModel);

  let whereQuery = parseWhere(requestQuery);
  whereQuery = updateWhereWithObjectIds(requestQuery, whereQuery, layerModel)

  xdmp.trace("GDS-DEBUG", "group by: " + groupByFields);
  xdmp.trace("GDS-DEBUG", "order by: " + orderByFields);

  // Hard code to 0 and max for now as these aren't technically supported for
  // the feature service aggregations but we may want to support limiting if there
  // are a lot of values.
  const offset = 0;
  const limit = Number.MAX_SAFE_INTEGER;

  const bindParams = {
    "offset" : offset,
    "limit" : limit
  };

  let pipeline;
  if (layerModel.dataSources === undefined) {
    const schema = layerModel.schema;
    const view = layerModel.view;

    xdmp.trace("GDS-DEBUG", "layerModel.dataSources === undefined, using " + defaultDocId + " as fragment id column");
    let viewPlan = op.fromView(schema, view, "", defaultDocId);

    pipeline = initializePipeline(viewPlan, boundingQuery, layerModel)
  } else {
    const primaryDataSource = layerModel.dataSources[0];
    if (primaryDataSource.source === "view") {
      const schema = primaryDataSource.schema;
      const view = primaryDataSource.view;

      // Dynamically choosing prefix depending on existance of fragment ID column.
      const prefix = primaryDataSource.fragmentIdColumn ? null : "";
      const fragmentIdColumn = primaryDataSource.fragmentIdColumn ? primaryDataSource.fragmentIdColumn : defaultDocId;
      xdmp.trace("GDS-DEBUG", "fragmentIdColumn: " + fragmentIdColumn);
      let viewPlan = op.fromView(schema, view, prefix, fragmentIdColumn);

      pipeline = initializePipeline(viewPlan, boundingQuery, layerModel)
    } else if (primaryDataSource.source === "sparql") {
      let viewPlan = getPlanForDataSource(primaryDataSource);
      pipeline = initializePipeline(viewPlan, boundingQuery, layerModel)
    }
  }

  pipeline = pipeline
    .where(boundingQuery)
    .where(whereQuery)
    .groupBy(
      groupByFields,
      getAggregateGroupByDef(stats)
    )
    .orderBy(getOrderByDef(orderByFields))
    .offset(op.param("offset"))
    .limit(op.param("limit"));

  return pipeline.result("object", bindParams);
};

function getAggregateFieldNames(aggregateDefs) {
  xdmp.trace("GDS-DEBUG", "Starting getAggregateFieldNames");
  return aggregateDefs.map((def) => {
    return def._outCol._colName;
  });
};


function getSelectDef(outFields, columnDefs, returnGeometry, geometryExtractor, exportPlan = false, idField="OBJECTID") {
  xdmp.trace("GDS-DEBUG", "Starting getSelectDef");
  if (exportPlan) {
    xdmp.trace("GDS-DEBUG", "Exporting Plan");
    return getExportPlanSelectDef(outFields, columnDefs)
  }

  // start with a GeoJSON feature with properties
  const defs = [
    op.as("type", "Feature"),
    op.as("id", op.col(idField)),
    op.as(
      "properties",
      op.jsonObject(getPropDefs(outFields, columnDefs))
    )
  ];

  xdmp.trace("GDS-DEBUG", "defs before returnGeometry");
  xdmp.trace("GDS-DEBUG", defs);

  // only include this if returnGeometry is true or outFields is *
  if (returnGeometry || outFields[0] === "*") {
    xdmp.trace("GDS-DEBUG", "Returning geometry from the following selector: ");
    xdmp.trace("GDS-DEBUG", geometryExtractor.getSelector());
    defs.push(geometryExtractor.getSelector());
  }
  return defs;
}

function getExportPlanSelectDef(outFields, columnDefs) {
  xdmp.trace("GDS-DEBUG", "Starting getExportPlanSelectDef");
  xdmp.trace("GDS-DEBUG", "outFields:");
  xdmp.trace("GDS-DEBUG", outFields);
  xdmp.trace("GDS-DEBUG", "coluumnDefs");
  xdmp.trace("GDS-DEBUG", columnDefs);

  const props = [];

  if (outFields.length === 0 || outFields[0] === "*") {
    // we need to select all the columns here
    // we can't just leave it blank to select everything though because
    // we are selecting other parts of the docs

    columnDefs.forEach((col) => {
      props.push(getSelectAs(col));
    });
  } else {
    outFields.forEach((f) => {
      xdmp.trace("GDS-DEBUG", "LOOKING FOR " + f)
      const col = columnDefs.find((c) => {
        xdmp.trace("GDS-DEBUG", Sequence.from(["looking at ", f, c]));
        return c.alias === f || c.name === f
      });
      if (col) props.push(getSelectAs(col));
    });
  }
  return props;
}

function getSelectAs(col) {
  xdmp.trace("GDS-DEBUG", "Starting getSelectAs");
  let colName;
  let alias;
  if (col.alias) {
    alias = col.alias;
    colName = col.name;
  }
  else if (col.name) {
    alias = col.name;
    colName = col.name;
  } else {
    colName = col;
    alias = col;
  }
  if (alias.toUpperCase() == colName.toUpperCase())
    return colName.toUpperCase();
  else
    return op.as(alias, op.col(colName));
}

function getPropDefs(outFields, columnDefs) {
  xdmp.trace("GDS-DEBUG", "Starting getPropDefs");
  const props = [];

  if (outFields.length === 0 || outFields[0] === "*") {
    // we need to select all the columns here
    // we can't just leave it blank to select everything though because
    // we are selecting other parts of the docs

    columnDefs.forEach((col) => {
      let colName;
      if (col.name === undefined) {
        colName = col;
      } else {
        colName = col.name;
      }
      xdmp.trace("GDS-DEBUG","***** ColName: " + colName )
      props.push(
        op.prop(
          colName,
          op.case(
            op.when(op.isDefined(op.col(colName)), op.col(colName)), op.jsonNull()
          )
        )
      )
    });
  } else {
    outFields.forEach((f) => {
      const col = columnDefs.find((c) => { return c.name === f });
      props.push(
        op.prop(
          col.name,
          op.case(
            op.when(op.isDefined(op.col(col.name)), op.col(col.name)), op.jsonNull()
          )
        )
      )
    });
  }
  return props;
}

function getValueConverter(col) {
  xdmp.trace("GDS-DEBUG", "Starting getValueConverter");
  switch (col.scalarType) {
    case "date":
      return "";
    default:
      return op.col(col.name)
  }
}

function getOrderByDef(fields) {
  xdmp.trace("GDS-DEBUG", "Starting getOrderByDef");
  return fields.map((field) => {
    switch (field.order.toLowerCase()) {
      case "desc":
        return op.desc(field.field);
      case "asc":
        return op.asc(field.field);
      default:
        return op.asc(field.field);
    }
  });
}

// not used any more since we don't return aggregates in properties
function getAggregatePropDefs(groupByFields, stats) {
  xdmp.trace("GDS-DEBUG", "Starting getAggregatePropDefs");
  const props = [];

  groupByFields.map((f) => {
    props.push(
      op.prop(f, op.col(f))
    )
  });

  stats.map((stat) => {
    props.push(
      op.prop(stat.outStatisticFieldName, op.jsonNumber(op.col(stat.outStatisticFieldName)))
    );
  });
  return props;
}

function getAggregateGroupByDef(stats) {
  xdmp.trace("GDS-DEBUG", "Starting getAggregateGroupByDef");
  return stats.map(getAggregateStatDef);
}

function getAggregateStatDef(stat) {
  xdmp.trace("GDS-DEBUG", "Starting getAggregateStatDef");
  const statsType = stat.statisticType;
  const statsFieldName = stat.onStatisticField;
  const statsOutFieldName = stat.outStatisticFieldName || stat.onStatisticField + "_" + statsType;

  //"statisticType": "<count | sum | min | max | avg | stddev | var>"
  switch (statsType.toLowerCase()) {
    case "count":
      return op.count(statsOutFieldName, statsFieldName);
    case "sum":
      return op.sum(statsOutFieldName, statsFieldName);
    case "min":
      return op.min(statsOutFieldName, statsFieldName);
    case "max":
      return op.max(statsOutFieldName, statsFieldName);
    case "avg":
      return op.avg(statsOutFieldName, statsFieldName);
    case "stddev":
      return op.uda(statsOutFieldName, statsFieldName, "native/varianceplugin", "stddev");
    case "var":
      return op.uda(statsOutFieldName, statsFieldName, "native/varianceplugin", "variance");
    default:
      returnErrToClient(
        501,
        "statsType not supported: " + statsType,
        statsType + " aggregations not supported"
      );
  }
}

exports.POST = post;
