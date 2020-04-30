'use strict';
const geojson = require('/MarkLogic/geospatial/geojson');
const geogml = require("/MarkLogic/geospatial/gml");
const geokml = require('/MarkLogic/geospatial/kml');
const georss = require('/MarkLogic/geospatial/georss');
const geo = require('/MarkLogic/geospatial/geospatial');
const mcgm = require('/MarkLogic/geospatial/mcgm');
const op = require('/MarkLogic/optic');


function getPointQuery(regions, layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: getPointQuery");
  const pointQueries = [];

  switch(layerModel.geometry.format) {
    case "geojson" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: geojson");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointOptions = [ "coordinate-system=" +coordinateSystem ]
      var localQuery =  geojson.geospatialQuery(regions,pointOptions)
      pointQueries.push(localQuery);
    }; break;
    case "gml" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: gml");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointFormat = layerModel.geometry.pointFormat;
      const pointOptions = [ "type=" +pointFormat , "coordinate-system=" +coordinateSystem ]
      var localQuery = geogml.geospatialQuery(regions,pointOptions)
      pointQueries.push(localQuery);
    } break;
    case "kml" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: kml");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointOptions = ["coordinate-system=" +coordinateSystem ]
      var localQuery = geokml.geospatialQuery(regions,pointOptions)
      pointQueries.push(localQuery);
    } break;
    case "rss" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: rss");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointOptions = ["coordinate-system=" +coordinateSystem ]
      var localQuery = georss.geospatialQuery(regions,pointOptions)
      pointQueries.push(localQuery);
    } break;
    case "mcgm" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: mcgm");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointOptions = ["coordinate-system=" +coordinateSystem ]
      const localQuery = cts.elementAttributePairGeospatialQuery(fn.QName("","Dot"), fn.QName("","Latitude"), fn.QName("","Longitude"), regions, pointOptions, 1)
      pointQueries.push(localQuery);
    } break;
    case "any" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: any");
      const coordinateSystem = layerModel.geometry.coordinateSystem;
      const pointOptions = ["coordinate-system=" +coordinateSystem ]
      const localQuery = geo.geospatialQuery(regions,pointOptions);
      pointQueries.push(localQuery);
    } break;
    case "custom" : {
      xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom");
      const indexes = layerModel.geometry.indexes;
      for (const key of Object.keys(indexes)) {
        switch (key) {
          case "element" :
          {
            xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom->element");
            const elementArray = layerModel.geometry.indexes.element
            for(let i=0; i < elementArray.length ; i++)
            {
              const pointFormat = elementArray[i].pointFormat
              const coordinateSystem = elementArray[i].coordinateSystem
              const namespaceURI  = elementArray[i].namespaceUri;
              const pointOptions = [ "type=" +pointFormat , "coordinate-system=" +coordinateSystem ]
              const elementLocalName = fn.QName(namespaceURI ,elementArray[i].localname)

              const localQuery = cts.elementGeospatialQuery(elementLocalName , regions, pointOptions)
              pointQueries.push(localQuery);
            }
          }
          break;
          case "elementChild" :
          {
            xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom->elementChild");
            const elementChildArray = layerModel.geometry.indexes.elementChild
            for(let i=0; i < elementChildArray.length ; i++)
            {
              const pointFormat = elementChildArray[i].pointFormat
              const coordinateSystem = elementChildArray[i].coordinateSystem
              const parentNamespace  = elementChildArray[i].parentNamespaceUri;
              const childNamespace = elementChildArray[i].namespaceUri;

              const pointOptions = [ "type=" +pointFormat , "coordinate-system=" +coordinateSystem ]
              const parentLocalname = fn.QName(parentNamespace ,elementChildArray[i].parentLocalname)
              const childElementName = fn.QName(childNamespace ,elementChildArray[i].localname)
              const localQuery = cts.elementChildGeospatialQuery(parentLocalname, childElementName, regions, pointOptions)
              pointQueries.push(localQuery);
            }
          }
          break;
          case "elementPair" :
          {
            xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom->elementPair");
            const elementPairArray = layerModel.geometry.indexes.elementPair
            for(let i=0; i < elementPairArray.length ; i++)
            {
              const parentNamespaceUri = elementPairArray[i].parentNamespaceUri;
              const parentLocalname = fn.QName(parentNamespaceUri,elementPairArray[i].parentLocalname);
              const latitudeNamespaceUri = elementPairArray[i].latitudeNamespaceUri;
              const latitudeLocalname = fn.QName(latitudeNamespaceUri,elementPairArray[i].latitudeLocalname);
              const longitudeNamespaceUri = elementPairArray[i].longitudeNamespaceUri;
              const longitudeLocalname = fn.QName(latitudeNamespaceUri,elementPairArray[i].longitudeLocalname);
              const coordinateSystem = elementPairArray[i].coordinateSystem;
              const pointOptions = ["coordinate-system=" +coordinateSystem ];

              const localQuery = cts.elementPairGeospatialQuery(parentLocalname, latitudeLocalname, longitudeLocalname, regions, pointOptions)
              pointQueries.push(localQuery);
            }
          }
          break;
          case "elementAttributePair" :
          {
            xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom->elementAttributePair");
            const elementAttributePairArray = layerModel.geometry.indexes.elementAttributePair
            for(let i=0; i < elementAttributePairArray.length ; i++)
            {
              const parentNamespaceUri = elementAttributePairArray[i].parentNamespaceUri;
              const parentLocalname = fn.QName(parentNamespaceUri,elementAttributePairArray[i].parentLocalname);
              const latitudeNamespaceUri = elementAttributePairArray[i].latitudeNamespaceUri;
              const latitudeLocalname = fn.QName(latitudeNamespaceUri,elementAttributePairArray[i].latitudeLocalname);
              const longitudeNamespaceUri = elementAttributePairArray[i].longitudeNamespaceUri;
              const longitudeLocalname = fn.QName(latitudeNamespaceUri,elementAttributePairArray[i].longitudeLocalname);
              const coordinateSystem = elementAttributePairArray[i].coordinateSystem;
              const pointOptions = ["coordinate-system=" +coordinateSystem ];

              const localQuery = cts.elementAttributePairGeospatialQuery(parentLocalname, latitudeLocalname, longitudeLocalname, regions, pointOptions)
              pointQueries.push(localQuery);
            }
          }
          break;
          case "path" :
          {
            xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom->path");
            const pathArray = layerModel.geometry.indexes.path;
            for(let i=0; i < pathArray.length ; i++)
            {
              const pointPaths = pathArray[i].pathExpression;
              const coordinateSystem = pathArray[i].coordinateSystem;
              const pointFormat = pathArray[i].pointFormat;
              const pointOptions = [ "type=" +pointFormat , "coordinate-system=" +coordinateSystem ]
              const localQuery = cts.pathGeospatialQuery(pointPaths,regions,pointOptions)
              pointQueries.push(localQuery);
            }
          }
          break;
          default:
          xdmp.trace("KOOP-DEBUG", "getPointQuery geometry format: custom TYPE NOT FOUND");
        }
      }
    }
  }

  return cts.orQuery(pointQueries);
}

function getRegionQuery(regions, operation, layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: getRegionQuery");
  xdmp.trace("KOOP-DEBUG", "In getRegionQuery");
  const geometry = layerModel.geometry;
  let regionPaths;

  xdmp.trace("KOOP-DEBUG", "geometry");
  xdmp.trace("KOOP-DEBUG", geometry);

  if (geometry.indexType && geometry.indexType === "region" && geometry.indexes) {
    xdmp.trace("KOOP-DEBUG", "In getRegionQuery if");
    // get the path references from the indexes configured for the layer
    // placeholder for this code
    // the conditions will probably need to be updated
  } else {
    xdmp.trace("KOOP-DEBUG", "In getRegionQuery else");
    regionPaths = [
      cts.geospatialRegionPathReference( '/envelope/ctsRegion', ['coordinate-system=wgs84'] )
    ];
  }

  const regionOptions = [];
  xdmp.trace("KOOP-DEBUG", "regionPaths:");
  xdmp.trace("KOOP-DEBUG", regionPaths);
  xdmp.trace("KOOP-DEBUG", "operation:");
  xdmp.trace("KOOP-DEBUG", operation);
  xdmp.trace("KOOP-DEBUG", "regions:");
  xdmp.trace("KOOP-DEBUG", regions);
  xdmp.trace("KOOP-DEBUG", "regionOptions:");
  xdmp.trace("KOOP-DEBUG", regionOptions);

  return cts.geospatialRegionQuery(regionPaths, operation, regions, regionOptions);
}

function CtsExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: CtsExtractor");
  this.getSelector = function () {
    return op.as("geometry", op.fn.string(op.xpath('doc', layer.geometry.source.xpath)));
  }

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    result.geometry = geojson.toGeojson(result.geometry);
    return result;
  }
}

function GeoJsonExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: GeoJsonExtractor");
  this.getSelector = function () {
    if (layer.geometry.source && layer.geometry.source.column) {
      xdmp.trace("KOOP-DEBUG", "op.xpath('doc'), from layer.geometry.source.column");
      xdmp.trace("KOOP-DEBUG", layer.geometry.source.column);
      return op.as("geometry", op.call('http://marklogic.com/xdmp', 'unquote', op.col(layer.geometry.source.column)));
    } else if (layer.geometry.source && layer.geometry.source.xpath) {
      xdmp.trace("KOOP-DEBUG", "op.xpath('doc'), from layer.geometry.source.xpath");
      xdmp.trace("KOOP-DEBUG", layer.geometry.source.xpath);
      return op.as("geometry", op.xpath('doc', layer.geometry.source.xpath));
    } else if (layer.geometry && layer.geometry.xpath) {
      xdmp.trace("KOOP-DEBUG", "op.xpath('doc'), from layer.geometry.xpath");
      xdmp.trace("KOOP-DEBUG", layer.geometry.xpath);
      return op.as("geometry", op.xpath('doc', layer.geometry.xpath));
    } else {
      xdmp.trace("KOOP-DEBUG", "WARNING: No GeoJSON configuration found");
    }
  }

  this.hasExtractFunction = function () {
    return false;
  }
}

function WKTExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: WKTExtractor");
  this.getSelector = function () {
    if (layer.geometry.source && layer.geometry.source.column) {
      xdmp.trace("KOOP-DEBUG", "WKTExtractor pulling data from layer.geometry.source.column");
      return op.as("geometry", op.col(layer.geometry.source.column));
    } else if (layer.geometry.source && layer.geometry.source.xpath) {
      xdmp.trace("KOOP-DEBUG", "WKTExtractor pulling data from layer.geometry.source.xpath");
      return op.as("geometry", op.xpath('doc', layer.geometry.source.xpath));
    } else if (layer.geometry && layer.geometry.xpath) {
      xdmp.trace("KOOP-DEBUG", "WKTExtractor pulling data from layer.geometry.xpath");
      return op.as("geometry", op.xpath('doc', layer.geometry.xpath));
    }
  }

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    result.geometry = geojson.toGeojson(geo.parseWkt(result.geometry));
    return result;
  }
}

function GMLExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: GMLExtractor");
  this.getSelector = function () {
    return op.as('geometry', op.jsonObject([
      op.prop("pointFormat", layer.geometry.pointFormat),
      op.prop("coordinateSystem", layer.geometry.coordinateSystem),
      op.prop("points", op.jsonArray(op.xpath("doc", "//Q{http://www.opengis.net/gml/3.2}Point/Q{http://www.opengis.net/gml/3.2}pos/node()")))
    ]))
  }

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    }

    if (result.geometry) {
      const extracted = result.geometry.toObject();
      const lonLat = (extracted.pointFormat === "long-lat-point");

      let points = extracted.points;
      for (const point of points) {
        const parts = point.valueOf().trim().split(/\s*,\s*|\s+/, 2);
        if (lonLat) {
          resultGeometry.coordinates.push([ Number(parts[0]), Number(parts[1])]);
        } else {
          resultGeometry.coordinates.push([ Number(parts[1]), Number(parts[0])]);
        }
      }

      result.geometry = resultGeometry;
    }
    return result;
  }
}

function KMLExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: KMLExtractor");
  this.getSelector = function () {
    const selector = op.jsonObject([
      op.prop("coordinateSystem", layer.geometry.coordinateSystem),
      op.prop("points", op.jsonArray(op.xpath("doc", "//Q{http://www.opengis.net/kml/2.2}Point/Q{http://www.opengis.net/kml/2.2}coordinates/node()")))
    ])
    return op.as('geometry', selector)
  }

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    }

    if (result.geometry) {
      const extracted = result.geometry.toObject();
      let points = extracted.points;

      for (const point of points) {
        const parts = point.valueOf().trim().split(/\s*,\s*|\s+/, 2);
        resultGeometry.coordinates.push([ Number(parts[0]), Number(parts[1])]);
      }

      result.geometry = resultGeometry;
    }
    return result;
  }
}

function RSSExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: RSSExtractor");
  this.getSelector = function () {
    const selector = op.jsonObject([
      op.prop("coordinateSystem", layer.geometry.coordinateSystem),
      op.prop("points", op.jsonArray(op.xpath("doc", "//item/Q{http://www.georss.org/georss}point/node()")))
    ])
    return op.as('geometry', selector)
  };

  this.hasExtractFunction = function () {
    return true;
  };

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    };

    if (result.geometry) {
      const extracted = result.geometry.toObject();
      let points = extracted.points;

      for (const point of points) {
        const parts = point.valueOf().trim().split(/\s*,\s*|\s+/, 2);
        resultGeometry.coordinates.push([ Number(parts[1]), Number(parts[0])]);
      }

      result.geometry = resultGeometry;
    }
    return result;
  };
}

function McgmExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: McgmExtractor");
  const lonLat = (layer.geometry.pointFormat === "long-lat-point");

  this.getSelector = function () {
    const values = op.jsonObject([
      op.prop("coordinateSystem", layer.geometry.coordinateSystem),
      op.prop('lats',op.map.entry("list", op.xpath("doc", "//Dot/@Latitude"))),
      op.prop('lons',op.map.entry("list", op.xpath("doc", "//Dot/@Longitude")))
    ]);
    return op.as('geometry', values);
  };

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    };
    if (result.geometry) {

      let pointsObj = result.geometry.toObject();

      let lats = pointsObj.lats;
      let lons = pointsObj.lons;

      if (lats.list) {
        lats = Array.isArray(lats.list) ? lats.list : [ lats.list ];
        lons = Array.isArray(lons.list) ? lons.list : [ lons.list ];
      }

      lats.forEach((lat, index) => {
        resultGeometry.coordinates.push([ Number(lons[index]), Number(lat) ]);
      });

      result.geometry = resultGeometry;
    }
    return result;
  };
}

function AnyExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: AnyExtractor");
  this.getSelector = function () {
    const selector = op.jsonObject([
      op.prop("coordinateSystem", layer.geometry.coordinateSystem),
      op.prop("points", op.jsonObject([
        op.prop("gml",op.jsonArray(op.xpath("doc", "//Q{http://www.opengis.net/gml/3.2}Point/Q{http://www.opengis.net/gml/3.2}pos/node()"))),
        op.prop("kml",op.jsonArray(op.xpath("doc", "//Q{http://www.opengis.net/kml/2.2}Point/Q{http://www.opengis.net/kml/2.2}coordinates/node()"))),
        op.prop("rss",op.jsonArray(op.xpath("doc", "//item/Q{http://www.georss.org/georss}point/node()"))),
        op.prop("mcgm",op.jsonObject([
          op.prop('lats', op.map.entry("list", op.xpath("doc", "//Dot/@Latitude")) ),
          op.prop('lons', op.map.entry("list", op.xpath("doc", "//Dot/@Longitude")) )
        ]))
      ]))
    ]);
    return op.as('geometry', selector)
  };

  this.hasExtractFunction = function () {
    return true;
  };

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    };

    if (result.geometry) {
      const extracted = result.geometry.toObject();

      let pointsObj = extracted.points;
      for (var key in pointsObj) {
        if (key == "mcgm"){
          let lats = pointsObj[key].lats;
          let lons = pointsObj[key].lons;

          // use the "list" property as a workaround for bug 49815
          if (lats.list) {
            lats = Array.isArray(lats.list) ? lats.list : [ lats.list ];
            lons = Array.isArray(lons.list) ? lons.list : [ lons.list ];

            lats.forEach((lat, index) => {
              resultGeometry.coordinates.push([ Number(lons[index]), Number(lat) ]);
            });
          }
        } else {
          const points = pointsObj[key]
          for (const point of points) {
            const parts = point.valueOf().trim().split(/\s*,\s*|\s+/, 2);
            if (key == "kml"){
              resultGeometry.coordinates.push([ Number(parts[0]), Number(parts[1])]);
            } else {
              resultGeometry.coordinates.push([ Number(parts[1]), Number(parts[0])]);
            }
          }
        }
      }
      result.geometry = resultGeometry;
    }
    return result;
  }
}

function CustomExtractor(layer) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: CustomExtractor");
  this.getSelector = function () {
    const indexes = layer.geometry.indexes;
    const selectors = [];

    xdmp.trace("KOOP-DEBUG", "Using the following layer geometry index settings");
    if (!indexes) xdmp.trace("KOOP-DEBUG", "Indexes are not defined");
    else xdmp.trace("KOOP-DEBUG", indexes);


    // path indexes
    if (indexes.path && indexes.path.length > 0 ) {
      xdmp.trace("KOOP-DEBUG", "Found path indexes to operate on.");
      for (const index of indexes.path) {
        xdmp.trace("KOOP-DEBUG", "Processing Path Index:");
        xdmp.trace("KOOP-DEBUG", index);
        selectors.push( op.jsonObject([
          op.prop("pointFormat", index.pointFormat),
          op.prop("coordinateSystem", index.coordinateSystem),
          // convert node value to string so the extractor can handle the values later
          op.prop("points", op.map.entry("list", op.call("http://www.w3.org/2005/xpath-functions", "string", op.xpath("doc", getPathXPath(index)))))
        ]));
      }
    }

    // element indexes
    if (indexes.element && indexes.element.length > 0 ) {
      xdmp.trace("KOOP-DEBUG", "Found element indexes to operate on.");
      for (const index of indexes.element) {
        xdmp.trace("KOOP-DEBUG", "Processing Element Index:");
        xdmp.trace("KOOP-DEBUG", index);
        selectors.push( op.jsonObject([
          op.prop("pointFormat", index.pointFormat),
          op.prop("coordinateSystem", index.coordinateSystem),
          op.prop("points", op.jsonArray(op.xpath("doc", getElementXPath(index))))
        ]));
      }
    }

    // element child indexes
    if (indexes.elementChild && indexes.elementChild.length > 0 ) {
      xdmp.trace("KOOP-DEBUG", "Found elementChild indexes to operate on.");
      for (const index of indexes.elementChild) {
        xdmp.trace("KOOP-DEBUG", "Processing element child Index:");
        xdmp.trace("KOOP-DEBUG", index);
        selectors.push( op.jsonObject([
          op.prop("pointFormat", index.pointFormat),
          op.prop("coordinateSystem", index.coordinateSystem),
          op.prop("points", op.jsonArray(op.xpath("doc", getElementChildXPath(index))))
        ]));
      }
    }


    // element pair indexes
    if (indexes.elementPair && indexes.elementPair.length > 0 ) {
      xdmp.trace("KOOP-DEBUG", "Found elementPair indexes to operate on.");
      for (const index of indexes.elementPair) {
        xdmp.trace("KOOP-DEBUG", "Processing element pair Index:");
        xdmp.trace("KOOP-DEBUG", index);
        selectors.push( op.jsonObject([
          op.prop("coordinateSystem", index.coordinateSystem),
          op.prop('lats',
          op.jsonArray(op.xpath("doc", getElementPairLatXPath(index)))
          ),
          op.prop('lons',
          op.jsonArray(op.xpath("doc", getElementPairLonXPath(index)))
          )
        ]));
      }
    }

    if (indexes.elementAttributePair && indexes.elementAttributePair.length > 0 ) {
      xdmp.trace("KOOP-DEBUG", "Found elementAttributePair indexes to operate on.");
      for (const index of indexes.elementAttributePair) {
        xdmp.trace("KOOP-DEBUG", "Processing element attribute pair Index:");
        xdmp.trace("KOOP-DEBUG", index);
        selectors.push( op.jsonObject([
          op.prop("coordinateSystem", index.coordinateSystem),
          op.prop('lats', op.map.entry("list", op.xpath("doc", getAttributePairLatXPath(index)))), // use map.entry as a workaround for bug 49815
          op.prop('lons', op.map.entry("list", op.xpath("doc", getAttributePairLonXPath(index))))  // use map.entry as a workaround for bug 49815
        ]));
      }
    }
    xdmp.trace("KOOP-DEBUG", "Built Selectors");
    xdmp.trace("KOOP-DEBUG", selectors);
    return op.as('geometry', selectors);
  }

  this.hasExtractFunction = function () {
    return true;
  }

  this.extract = function (result) {
    const resultGeometry = {
      type : "MultiPoint",
      coordinates : []
    };

    if (result.geometry) {
      xdmp.trace("KOOP-DEBUG", "Processing result.geometry");
      xdmp.trace("KOOP-DEBUG", result.geometry);
      
      let geometryToProcess = result.geometry.toObject();
      if (Array.isArray(geometryToProcess)) {
        xdmp.trace("KOOP-DEBUG", "result.geometry is an array, expecting to extract multiple points");
      }
      else {
        xdmp.trace("KOOP-DEBUG", "result.geometry is an object, expecting to extract single point");
        geometryToProcess = [ geometryToProcess ];
        resultGeometry.type = "Point";
      }

      for (const geometry of geometryToProcess) {
        const extracted = geometry instanceof Node ? geometry.toObject() : geometry;

        xdmp.trace("KOOP-DEBUG", "Extracted geometry");
        xdmp.trace("KOOP-DEBUG", extracted);

        if (extracted.points) {
          xdmp.trace("KOOP-DEBUG", "Processing points");
          xdmp.trace("KOOP-DEBUG", extracted.points);
          let points = extracted.points;
          if (points.list) {
            points = Array.isArray(points.list) ? points.list : [ points.list ];
          }

          const lonLat = (extracted.pointFormat === "long-lat-point");
          if (Array.isArray(points)){
            for (const point of points) {
              if (point === null) { continue; }
              const parts = point.valueOf().trim().split(/\s*,\s*|\s+/, 2);
              if (lonLat) {
                resultGeometry.coordinates.push([ Number(parts[0]), Number(parts[1])]);
              } else {
                resultGeometry.coordinates.push([ Number(parts[1]), Number(parts[0])]);
              }
            }
          }
        } else if (extracted.lats) {
          let lats = extracted.lats;
          let lons = extracted.lons;

          xdmp.trace("KOOP-DEBUG", "Processing Lat/Lon");
          xdmp.trace("KOOP-DEBUG", extracted.lats);
          xdmp.trace("KOOP-DEBUG", extracted.lons);

          // use the "list" property as a workaround for bug 49815
          if (lats.list) {
            lats = Array.isArray(lats.list) ? lats.list : [ lats.list ];
            lons = Array.isArray(lons.list) ? lons.list : [ lons.list ];
          }

          if(lats.length > 0){
            lats.forEach((lat, index) => {
              resultGeometry.coordinates.push([ Number(lons[index]), Number(lat) ]);
            });
          }
        }
      }
      if (resultGeometry.type === "Point") {
        if (resultGeometry.coordinates.length >= 1) {
          resultGeometry.coordinates = resultGeometry.coordinates[0]; // remove multi-dimensional array / there should only be 1 coordinate for "Point"
        }
      }
      result.geometry = resultGeometry;
    }
    return result;
  }
}

function ns(uri) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: ns");
  if (uri) {
    return `Q{${uri}}`;
  } else {
    return "";
  }
}

function getPathXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getPathXPath");
  // check to see if the path returns an attribute
  // TODO: this likely needs to be exanded as it only does very basic detection
  // maybe we want the user to specify in the config that the path index selects an attribute?
  const isAttribute = index.pathExpression.split(/\/ *\(? *@/).length > 1;

  // TODO: we need to document inconsistencies between XPath supported by indexes and those that work here
  // e.g. don't end a path with node() or text() or something that returns a primitive
  return isAttribute ? index.pathExpression : index.pathExpression;
}

function getElementXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getElementXPath");
  return `//${ns(index.namespaceUri)}${index.localname}/node()`;
}

function getElementChildXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getElementChildXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/${ns(index.namespaceUri)}${index.localname}/node()`;
}

function getElementPairLatXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getElementPairLatXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/${ns(index.latitudeNamespaceUri)}${index.latitudeLocalname}/node()`;
}

function getElementPairLonXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getElementPairLonXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/${ns(index.longitudeNamespaceUri)}${index.longitudeLocalname}/node()`;
}

function getElementPairLatXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getElementPairLatXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/${ns(index.latitudeNamespaceUri)}${index.latitudeLocalname}/node()`;
}

function getAttributePairLonXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getAttributePairLonXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/@${ns(index.longitudeNamespaceUri)}${index.longitudeLocalname}`;
}

function getAttributePairLatXPath(index) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getAttributePairLatXPath");
  return `//${ns(index.parentNamespaceUri)}${index.parentLocalname}/@${ns(index.latitudeNamespaceUri)}${index.latitudeLocalname}`;
}

function getExtractFunction(layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor helper function: getExtractFunction");
  let format = layerModel.geometry.format;
  if (layerModel.geometry.source && layerModel.geometry.source.format) {
    format = layerModel.geometry.source.format;
  }

  switch(format) {
    case "geojson" : { return new GeoJsonExtractor(layerModel) } break;
    case "wkt"     : { return new WKTExtractor(layerModel) }     break;
    case "gml"     : { return new GMLExtractor(layerModel) }     break;
    case "kml"     : { return new KMLExtractor(layerModel) }     break;
    case "rss"     : { return new RSSExtractor(layerModel) }     break;
    case "mcgm"    : { return new McgmExtractor(layerModel) }    break;
    case "custom"  : { return new CustomExtractor(layerModel) }  break;
    case "any"     : { return new AnyExtractor(layerModel) }     break;
    case "cts"     : { return new CtsExtractor(layerModel) }     break;
    default: xdmp.trace("KOOP-DEBUG", `ALERT: Unknown format in getExtractFunction. format = ${format}`);
  }
}

function getExtractor(layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: getExtractor");
  return getExtractFunction(layerModel);
}

function getSelector(layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: getSelector");
  return getExtractFunction(layerModel).getSelector();
}

function getMapper(layerModel) {
  xdmp.trace("KOOP-DEBUG", "Using the following extractor: getMapper");
  return getExtractFunction(layerModel).extract;
}

exports.getPointQuery = getPointQuery;
exports.getRegionQuery = getRegionQuery;
exports.getExtractor = getExtractor;
exports.getSelector = getSelector;
exports.getMapper = getMapper;
