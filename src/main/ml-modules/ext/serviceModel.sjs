'use strict';
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');
const sfc = require('/ext/server-field-cache.xqy');

const SERVICE_DESCRIPTOR_COLLECTION = 'http://marklogic.com/feature-services';
const MAX_RECORD_COUNT = 5000;

const DEBUG = xdmp.traceEnabled("GDS-DEBUG");

var serviceModelIndex = {
  
};

var viewHashIndex = {
  
}

function getServiceModels(filter) {
  const validFilters = new Set(['all', 'search', 'geoserver']);
  const _filter = filter || 'all';
  if (!validFilters.has(_filter)) { throw err.newInputError(`Invalid filter '${_filter}'.`)}

  //clear the serviceModelIndex
  for (let i in serviceModelIndex) delete serviceModelIndex[i];

  // (re)select all descriptors and cache them
  let allModels = [];
  let additionalQueries = [cts.trueQuery()];
  if (_filter == 'geoserver')
    additionalQueries.push(cts.jsonPropertyScopeQuery("geoServerMetadata", cts.trueQuery()));
  else if (_filter == 'search') 
    additionalQueries.push(cts.jsonPropertyScopeQuery("search", cts.trueQuery()));
  
  for (let d of cts.search(cts.andQuery([cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION), ...additionalQueries]))) {
    setServiceModelIndexEntry(d);
  }

  for (let i in serviceModelIndex) {
      allModels.push(serviceModelIndex[i].serviceModel);
  }
  trace.info(`Found a total of ${allModels.length} service descriptor documents.`, "getServiceModels");

  if (_filter === 'search')
    return allModels.filter(m => m.search);
  else if (_filter === 'geoserver')
    return allModels.filter(m => m.layers && Array.isArray(m.layers) && m.layers.some(l => l.geoServerMetadata));
  else
    return allModels;
}

function getServiceModel(serviceId) {
    let entry = getServiceModelIndexEntry(serviceId, true);
    return entry.serviceModel;
}

function setLayerModelIndexEntry(serviceId, layer) {
    let entry = getServiceModelIndexEntry(serviceId);
    entry.layerModelIndex[layer.id] = layer;
}

function viewHashesValid(layerModelEntry) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting viewHashesValid");
    if (layerModelEntry) {
        let dbLayerViewHashes = getViewHashes(layerModelEntry);
        let descriptorLayerViewHashes = layerModelEntry.viewHashes;

        //check that layer model's view hashes is the same length as what we 
        //pulled out of the local index
        if (Object.keys(dbLayerViewHashes).length != Object.keys(descriptorLayerViewHashes).length) {
            if (DEBUG) xdmp.trace("GDS-DEBUG", "# of viewHashes are different: dbLayerViewHashes.length = " + Object.keys(dbLayerViewHashes).length + " descriptorLayerViewHashes.length = " + Object.keys(descriptorLayerViewHashes).length);
            return false;
        }
        
        //if they have the same # of hashes, loop through each hash and compare
        //them, if any one is different then return false
        if (Object.keys(descriptorLayerViewHashes).some(hash => {
            descriptorLayerViewHashes[hash] != dbLayerViewHashes[hash]
        })) {
            if (DEBUG) xdmp.trace("GDS-DEBUG", "descriptor's viewHashes don't match current view hashes");
            return false;
        }
        if (DEBUG) xdmp.trace("GDS-DEBUG", "all viewHashes valid");
        return true;
    }
    else {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "layerModelEntry is null, returning false");
        return false;
    }
}

function getLayerModelIndexEntryFromDb(serviceModelEntry, layerId) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting getLayerModelIndexEntryFromDb");
    /*
        First we'll look for a document metadata field with the layer info.  This
        call is really fast.  If we find the layer model, validate its hashes and 
        return it.  Retrieve it in a separate transaction bc we may have stored it back
        to disk earlier in this transaction.  The layerModelEntry structure looks like this:
        {
            "layerModel": {...the usual layer model here},
            "viewHashes": {
                "mySchemaName.myViewName":"1234123412341234",
                "mySchemaName.myOtherViewName":"4567456745674567"
            }
        }
        */
    let layerMetadataField =
        fn.head(xdmp.invokeFunction(function () {
            return xdmp.documentGetMetadataValue(serviceModelEntry.uri, "layer_" + layerId + "_model");
        }, { isolation: "different-transaction" }
        ));

    if (layerMetadataField) {
        let layerModelEntry = JSON.parse(layerMetadataField);
        if (viewHashesValid(layerModelEntry)) {
            serviceModelEntry.layerModelIndex[layerId] = layerModelEntry;
            return layerModelEntry;
        }
    }
    //if we get here that means either there was no layerMetadataField stored
    //to begin with or the view hashes changed.  Returning null will force it
    //to be generated and stored to disk in a metadata field.
    return null;
}

function calculateNewLayerModelIndex(serviceModel, layerId) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "starting calculateNewLayerModelIndex");
    //if we got here, that means there was no pre-generated layer model, or 
    //the layer's view hashes didn't match the database's.  So build it
    //and return it.
    let layer = null;
    let serviceName = null;
    if (serviceModel) {
        layer =
            serviceModel.layers.find((l) => {
                return l.id == layerId;
            });
        serviceName = serviceModel.info.name;
    }

    if (layer) {
        // default the schema to the service id if not specified
        layer.schema = layer.schema || serviceName;

        // default the geometry to GeoJSON
        if (!layer.geometry) {
            layer.geometry = {
                type: "Point",
                format: "geojson",
                coordinateSystem: "wgs84",
                xpath: "//geometry"
            };
        }

        let columnDefs = generateFieldDescriptors(layer, serviceModel.name);
        
        let layerModelIndexEntry = {
            layerId:layer.id,
            layerModel:layer,
            columnDefs:columnDefs
        };

        let viewHashes = getViewHashes(layerModelIndexEntry);
        layerModelIndexEntry.viewHashes = viewHashes;

        if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["layerModelIndexEntry:", layerModelIndexEntry]));
        return layerModelIndexEntry;
    } else {
        throw "Layer " + layerId + " not found.";
    }
}

function getColumnDefs(serviceName, layerId) {
  let layerModelIndexEntry = getLayerModelIndexEntry(serviceName, layerId);
  return layerModelIndexEntry.columnDefs;
}

function saveLayerModelIndexEntryInDb(uri, layerModelIndexEntry) {
    xdmp.invokeFunction(function() {
        declareUpdate();
        let metadata = xdmp.documentGetMetadata(uri) || {};
        metadata["layer_" + layerModelIndexEntry.layerId + "_model"] = layerModelIndexEntry;
        xdmp.documentSetMetadata(uri, metadata);
    },
    {isolation:"different-transaction"});
}

function getLayerModelIndexEntry(serviceName, layerId) {
    let serviceModelEntry = null;
    try {
        serviceModelEntry = getServiceModelIndexEntry(serviceName, false);
    }
    catch(e) {
        xdmp.log(e);
        throw "Service " + serviceName + " not found.";
    }

    //look in the local transaction index object first, if we find it just return it
    let layerModelEntry = serviceModelEntry.layerModelIndex[layerId];
    if (layerModelEntry != null) {
        return layerModelEntry;
    }
    else {
        //look for it in the db metadata field and use that if it exists and is current
        layerModelEntry = getLayerModelIndexEntryFromDb(serviceModelEntry, layerId);
        if (layerModelEntry) 
            return layerModelEntry;
        else {
            //it's either not in the db or not current, so build a new one
            let serviceModel = getServiceModel(serviceModel);
            layerModelEntry = calculateNewLayerModelIndex(serviceModel, layerId);
            //save it in the db
            saveLayerModelIndexEntryInDb(serviceModelEntry.uri, layerModelEntry);
            //set it in the transaction cache
            serviceModelEntry.layerModelIndex[layerId] = layerModelEntry;
            return layerModelEntry;
        }
    }
}


function getServiceModelIndexEntry(serviceId, deep=false) {
  //look in the transaction index first and return it if it's there.
  if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): Looking for serviceId " + serviceId);
  if (serviceModelIndex[serviceId]) {
    let entry = serviceModelIndex[serviceId];
    if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): serviceModelIndex[serviceId] found");
    if (DEBUG) xdmp.trace("GDS-FINE", serviceModelIndex[serviceId]);

    //if we're not looking for the full model doc return it.  Or, 
    //if we're looking for the full model doc and we've already retrieved it,
    //return it.  Otherwise fall into the next block of code where we'll
    //retrieve the full doc.
    if ((!deep) || (deep && entry.serviceModel != null))
      return entry;
  }
  let modelQuery = cts.andQuery([
    cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
    cts.jsonPropertyValueQuery("name", serviceId, ["exact"])
  ]);

  if (deep) {
    //if not, we always run a cts:search.  If we recently ran a search for the doc, it'll
    //still be in cache and is just as fast as a server field, and if the doc changed 
    //we'll get the latest copy
    if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): serviceModelIndex[serviceId] not found, executing search");
    const modelDoc = fn.head(
      cts.search(modelQuery)
    );

    //set the index entry.  This is for speed within this transaction
    //since the code looks for the service model/descriptor potentially serveral times
    //within the span of one transaction
    setServiceModelIndexEntry(modelDoc);
  }
  else {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): serviceModelIndex[serviceId] not found, creating shallow index entry");
    
    //in this case we just make a shell entry to hold the layer index.  This
    //avoids pulling the full descriptor doc.
    let uri = fn.head(cts.uris(null, null, modelQuery));
    if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): serviceModelIndex[serviceId]: uri = " + uri);
    setServiceModelIndexEntry(null, uri, serviceId);
  }
  if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): returning index entry");
  return serviceModelIndex[serviceId];
}

function setServiceModelIndexEntry(modelDoc, uri = null, serviceName = null) {
  if (modelDoc == null && uri != null) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "setServiceModelIndexEntry(); creating shallow index entry")
    serviceModelIndex[serviceName] = {
      serviceModel:null,
      serviceDescriptor:null,
      layerModelIndex:{},
      uri:uri
    };
  } 
  else {
    let modelObj = modelDoc.toObject();
    let serviceId = modelObj.info.name;
    if (serviceModelIndex[serviceId] != null) {
      serviceModelIndex[serviceId].serviceModel = modelObj;
    }
    else {
      serviceModelIndex[serviceId] = {
          serviceModel:modelObj,
          serviceDescriptor:null,
          layerModelIndex:{},
          uri:xdmp.nodeUri(modelDoc)
        };
    }
  }
}

function getLayerModel(serviceName, layerId) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting getLayerModel");

  //const serviceModel = getServiceModel(serviceName);
  let layerModelIndexEntry = getLayerModelIndexEntry(serviceName, layerId);

  return layerModelIndexEntry.layerModel;
}

function setFieldDescriptors(layer, serviceModel) {
  if (layer.gdsMetadata == null || layer.gdsMetadata.columnDefs == null || viewUpdated(layer)) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "view(s) updated, regenerating field descriptors");
    let columnDefs = generateFieldDescriptors(layer, serviceName)
    if (layer.gdsMetadata == null) layer.gdsMetadata = {};
    layer.gdsMetadata.viewHashes = getViewHashes(layer);
    layer.gdsMetadata.columnDefs = columnDefs;

    // xdmp.invokeFunction(function() {
    //   saveServiceModel(serviceName, serviceModel);
    // },
    // {isolation:"different-transaction"}
    // );
  }
}

function saveServiceModel(serviceId, model, uri) {
  declareUpdate();
  // TODO: add validate model before saving
  if (model == null) {
    throw "model is null";
  }
  
  let _uri;
  let _model = model;
  
  if (serviceModelIndex[serviceId] == null && uri != null) {
    //this is a brand new model
    _uri = uri;
  }
  else if (serviceModelIndex[serviceId] != null && (uri == null || uri == undefined)) {
    //we loaded the model from disk in this transaction, get the uri from the serviceModelIndex
    _uri = xdmp.nodeUri(serviceModelIndex[serviceId]);
  }
  else {
    //we have some situation we don't expect
    throw "serviceId '" + serviceId + "': invalid input parameters: model = " + (model == null ? " null; " : " not null; ") + "uri == " + (uri == null ? "null" : uri) + "  Check input parameters and that serviceId " + serviceId + " doesn't already exist";
  }
  
  if (DEBUG) xdmp.trace("GDS-DEBUG", `Updating service descriptor document \"${serviceId}\" at \"${_uri}\".`);

  //if the doc doesn't exist yet, be sure to add the service descriptor collection to the list of collections
  let collections = xdmp.documentGetCollections(_uri);
  if (!collections.includes(SERVICE_DESCRIPTOR_COLLECTION)) 
    collections.push(SERVICE_DESCRIPTOR_COLLECTION);
  
  //if the doc doesn't exist yet, at least set default permissions on it
  let permissions = xdmp.documentGetPermissions(_uri);
  if (permissions.length == 0) 
    permissions = xdmp.defaultPermissions();

  const options = {
    collections: collections,
    permissions: permissions,
    metadata: xdmp.documentGetMetadata(_uri),
    quality: xdmp.documentGetQuality(_uri)
  };

  serviceModelIndex[_uri] = _model;
  xdmp.documentInsert(_uri, _model, options);
}

function getCachedViewHash(viewNameKey) {
    let cachedViewHash = fn.head(sfc.get(viewNameKey));
    //if it's in the serer field cache, use that
    if (cachedViewHash) {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "Found viewHash in server field cache, " + viewNameKey + ": " + cachedViewHash);
        viewHashIndex[viewNameKey] = cachedViewHash;
    }
    return cachedViewHash;
}

function getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewInfo) {
    //if it's in the transaction's index, use that
    if (DEBUG) xdmp.trace("GDS-DEBUG", "Looking for viewHash for " + viewNameKey + " in transaction cache...");
    if (viewHashIndex[viewNameKey] != null) {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "Found viewHash in transaction cache, " + viewNameKey + ": " + viewHashIndex[viewNameKey]);
        viewHashes[viewNameKey] = viewHashIndex[viewNameKey];
    }
    //otherwise look in the server field cache
    else {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "Looking for viewHash for " + viewNameKey + " in server field cache...");
        let cachedViewHash = getCachedViewHash(viewNameKey);
        if (cachedViewHash) {
            viewHashes[viewNameKey] = cachedViewHash;
        }
        //if not, we have to calculate it
        else {
            if (DEBUG) xdmp.trace("GDS-DEBUG", "Deferring calculation of viewHash for " + viewNameKey + "...");
            viewInfo.push(viewNameKey);
        }
    }
}

/**
 * 
 * @param layerModelIndexEntry the layer index entry.
 * The intent of this function is to return the locally cached
 * current hashes for the views referenced by the layer.  We will
 * evaluate whether they match the layer's pre-calculated hashes in
 * the viewUpdated() function.
 */
function getViewHashes(layerModelIndexEntry) {
    let layer = layerModelIndexEntry.layerModel;
    let viewInfo = [];
    let viewHashes = {};

    //If the layerModel index entry already has hashes pre-calculated, we'll
    //look at those and return them
    if (layerModelIndexEntry.viewHashes != null && Object.keys(layerModelIndexEntry.viewHashes).length > 0) {
        for (let viewNameKey in layerModelIndexEntry.viewHashes) {
            getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewInfo);
        }
    }
    //if there are no viewHashes, we have to look to see if there
    //*should* be viewHashes
    else {
        if (layer.source == "view" || layer.dataSources == null) {
            let viewNameKey = layer.schema + "." + layer.view;
            getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewInfo);
        }
        if (layer.datasources && Array.isArray(layer.datasources)) {
          for (let ds of layer.dataSources) {
              if (ds.source == "view") {
                  let viewNameKey = ds.schema + "." + ds.view;
                  getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewInfo);
              }
          }
        }
    }

    if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["viewInfo:", viewInfo, "viewHashes:", viewHashes]));
    if (viewInfo.length > 0) {
        //only go calculate hashes if we haven't done so already in this transaction
        //TODO:  !!!!if we got here, we need to update the layerModel document metadata!!!!
        if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["calculating view hashes", viewInfo]));
        let retrievedViewHashes = fn.head(xdmp.invoke("/ext/view-hash.xqy", { input: Sequence.from(viewInfo) }));
        if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["retrieved view hashes", retrievedViewHashes]));
        for (let key in retrievedViewHashes) {
            let hashStr = retrievedViewHashes[key].toString()
            viewHashIndex[key] = hashStr;
            viewHashes[key] = hashStr;
            sfc.put(key, hashStr);
        }
    }
    else {
        if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["No view hashes to calculate; using cached viewHashes: ", viewHashes]));
    }
    return viewHashes;
}

function viewUpdated(layerModelIndexEntry) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting viewUpdated");
  if (layerModelIndexEntry.viewHashes == null)
    return true;
  
  let viewHashes = getViewHashes(layerModelIndexEntry);
  if (Object.keys(viewHashes).length != Object.keys(layerModelIndexEntry.viewHashes).length) {
    if (xdmp.traceEnabled("GDS-DEBUG")) {
      if (DEBUG) xdmp.trace("GDS-DEBUG", "computed viewhashes length:");
      if (DEBUG) xdmp.trace("GDS-DEBUG", Object.keys(viewHashes).length);
      if (DEBUG) xdmp.trace("GDS-DEBUG", "layer viewhashes length:");
      if (DEBUG) xdmp.trace("GDS-DEBUG", Object.keys(layerModelIndexEntry.viewHashes).length);
    }
    return true;
  }
  for (let key in viewHashes) {
    if (layerModelIndexEntry.viewHashes[key] == null || !viewHashes[key].equals(layerModelIndexEntry.viewHashes[key])) {
      if (xdmp.traceEnabled("GDS-DEBUG")) {
        xdmp.trace("GDS-DEBUG", "viewhash different");
        xdmp.trace("GDS-DEBUG", layerModelIndexEntry.viewHashes);
        xdmp.trace("GDS-DEBUG", viewHashes);
      }
      return true;
    }
  }
if (DEBUG) xdmp.trace("GDS-DEBUG", "layer views up-to-date");
return false;
}

function getSchema(layerDesc, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting getSchema");
  return layerDesc.schema || serviceName;
}

function generateFieldDescriptors(layerModel, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateFieldDescriptors");
  if (layerModel.view === undefined) {
    return generateFieldDescriptorsFromDataSourcesArray(layerModel, serviceName);
  } else {
    return generateFieldDescriptorsFromViewAndJoins(layerModel, serviceName);
  }
}

function generateFieldDescriptorsFromViewAndJoins(layerModel, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateFieldDescriptorsFromViewAndJoins");
  const fields = [];

  const schema = getSchema(layerModel, serviceName);
  const view = layerModel.view;
  const viewDef = tde.getView(schema, view);
  fields.push(...generateFieldDescriptorsFromViewDef(viewDef, layerModel));

  if (layerModel.joins) {
    fields.push(...generateJoinFieldDescriptorsFromViewAndJoins(layerModel));
  }

  return fields;
}

function generateFieldDescriptorsFromDataSourcesArray(layerModel, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateFieldDescriptorsFromDataSourcesArray");
  const fields = [];

  const primaryDataSource = layerModel.dataSources[0];
  if (primaryDataSource.source === "view") {
    const schema = getSchema(primaryDataSource, serviceName);
    const view = primaryDataSource.view;
    const viewDef = tde.getView(schema, view);
    fields.push(...generateFieldDescriptorsFromViewDef(viewDef, primaryDataSource));
  } else if (primaryDataSource.source === "sparql") {
    fields.push(...generateJoinFieldDescriptorsFromDataSource(primaryDataSource));
  }

  if (layerModel.dataSources.length > 1) {
    layerModel.dataSources.forEach((dataSource, index) => {
      if (index < 1) return;  // skip first element since it is the primary source
      if (dataSource.fields) {
        fields.push(...generateJoinFieldDescriptorsFromDataSource(dataSource));
      } else {
        const viewDef = tde.getView(dataSource.schema, dataSource.view);
        fields.push(...generateFieldDescriptorsFromViewDef(viewDef, dataSource));
      }
    });
  }

  return fields;
}

function generateJoinFieldDescriptorsFromViewAndJoins(layerModel) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateJoinFieldDescriptorsFromViewAndJoins");
  const fields = [];
  layerModel.joins.forEach((dataSource) => {
    Object.keys(dataSource.fields).forEach((field) => {
      if (layerModel.includeFields === undefined || layerModel.includeFields.includes(field)) {
        fields.push(createFieldDescriptor(field, dataSource.fields[field].scalarType, dataSource.fields[field].alias));
      }
    });
  });
  return fields;
}

function generateJoinFieldDescriptorsFromDataSource(dataSource) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateJoinFieldDescriptorsFromDataSource");
  const fields = [];
  Object.keys(dataSource.fields).forEach((field) => {
    if (dataSource.includeFields === undefined || dataSource.includeFields.includes(field)) {
      fields.push(createFieldDescriptor(field, dataSource.fields[field].scalarType, dataSource.fields[field].alias, dataSource.includeFields));
    }
  });
  return fields;
}

function generateFieldDescriptorsFromViewDef(viewDef, dataSource) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateFieldDescriptorsFromViewDef");
  const fields = [];
  for (const field of viewDef.view.columns) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "working on field " + field.column.name);
    if (dataSource.includeFields === undefined || dataSource.includeFields.includes(field.column.name)) {
      let alias = null;
      if (dataSource.hasOwnProperty("fields")) {
        if (dataSource.fields.hasOwnProperty(field.column.name)) {
          if (dataSource.fields[field.column.name].hasOwnProperty("alias")) {
            alias = dataSource.fields[field.column.name].alias;
          }
        }
      }
      fields.push(createFieldDescriptor(field.column.name, field.column.scalarType, alias, dataSource.includeFields));
    }
  };
  return fields;
}

function createFieldDescriptor(fieldName, scalarType, alias, includeFields) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting createFieldDescriptor");
  let fieldDescriptor = {
    name : fieldName,
    type : getFieldType(scalarType)
  };
  if (fieldDescriptor.type === "String") {
    fieldDescriptor.length = 1024;
  }
  if (alias) {
    fieldDescriptor.alias = alias;
  }
  return fieldDescriptor;
}

function getFieldType(datatype) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting getFieldType");
  switch(datatype) {
    case "anyURI":
    case "iri":
      return "string";
    case "duration":
    case "dayTimeDuration":
    case "yearMonthDuration":
    case "gDay":
    case "gMonth":
    case "gMonthDay":
    case "gYear":
    case "gYearMonth":
      return "String";
    case "hexBinary":
    case "base64Binary":
      return "String";
    case "boolean":
      return "Integer";
    case "string":
      return "String";
    case "byte":
    case "unsignedByte":
      return "Integer";
    case "time":
      return "Date";
    case "date":
      return "Date";
    case "dateTime":
      return "Date";
    case "short":
    case "unsignedInt":
    case "int":
    case "unsignedLong":
    case "integer":
    case "unsignedShort":
    case "long":
    case "nonNegativeInteger":
      return "Integer";
    case "nonPositiveInteger":
    case "negativeInteger":
      return "Integer";
    case "float":
    case "decimal":
    case "double":
      return "Double";
    case "array":
      return "String";
    default:
      return "String";
  }
}

function generateLayerDescriptor(serviceName, layerNumber) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateLayerDescriptor");
  if (DEBUG) xdmp.trace("GDS-DEBUG", "generating layer descriptor for " + serviceName + ":" + layerNumber);

  const serviceDesc = generateServiceDescriptor(serviceName, layerNumber);

  // find the layer we need
  const layer = serviceDesc.layers.find((l) => {
    return l.metadata.id == layerNumber;
  });

  if (layer) {
    return layer;
  } else {
    throw "No layer number " + layerNumber + " found";
  }
}


function generateServiceDescriptor(serviceName, layerNumber) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateServiceDescriptor");

  let serviceModelIndexEntry = getServiceModelIndexEntry(serviceName, true);

  if (serviceModelIndexEntry.descriptor == null) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "generating service descriptor for " + serviceName + (layerNumber == null || layerNumber == undefined ? ", all layers" : ", layer " + layerNumber));
    serviceModelIndexEntry.descriptor = transformServiceModelToDescriptor(serviceModelIndexEntry.serviceModel, serviceName, layerNumber);
  } 
  else {
    if (DEBUG) xdmp.trace("GDS-DEBUG", "using cached service descriptor for " + serviceName);
    //the descriptor was previously generated, but 
    //if we're looking for all layers, we have to make sure that all of them have
    //already been generated
    let serviceModel = serviceModelIndexEntry.serviceModel;
    let descriptor = serviceModelIndexEntry.descriptor;

    //if we're asking for the full service descriptor, build any layer descriptors that are missing
    if (layerNumber == null || layerNumber == undefined) {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "verifing cached service descriptor has all layers in it...");
        let sortLayers = false;
        for (let layerModel of serviceModel.layers) {
            if (!descriptor.layers.some(l => l.metadata.id == layerModel.id)) {
                if (DEBUG) xdmp.trace("GDS-DEBUG", "cached service descriptor for " + serviceName + " missing layer " + layerNumber + ", generating new layer descriptor");
                let newLayer = _buildOneLayerDescriptor(serviceName, layerModel);
                descriptor.layers.push(newLayer);
                sortLayers = true;
            }
        }
        //then sort the layers array by layer id if needed
        if (sortLayers) 
            descriptor.layers = descriptor.layers.sort(function(l1, l2) {return l1.metadata.id - l2.metadata.id;});
    }
    else {
        //if we're looking for a particular layer #, we need to make
        //sure we've got it in the descriptor and if not add it
        if (!descriptor.layers.some(l => l.metadata.id == layerNumber)) {
            if (DEBUG) xdmp.trace("GDS-DEBUG", "cached service descriptor for " + serviceName + " missing layer " + layerNumber + ", generating new layer descriptor");
            let layerModel = serviceModel.layers.find(l => l.id == layerNumber);
            if (!layerModel) 
                throw "Layer " + layerNumber + " not found for service " + serviceName;
            let newLayer = _buildOneLayerDescriptor(serviceName, layerModel);
            descriptor.layers.push(newLayer);
            descriptor.layers = descriptor.layers.sort(function(l1, l2) {return l1.metadata.id - l2.metadata.id;});
        }
    }
  }
  return serviceModelIndexEntry.descriptor;
}


function transformServiceModelToDescriptor(serviceModel, serviceName, layerNumber) {
  const desc = {
    description: serviceModel.info.description,
    maxRecordCount: MAX_RECORD_COUNT
  };

  // copy all the properties from the info section
  for (let propName in serviceModel.info) {
    desc[propName] = serviceModel.info[propName];
  }

  desc.layers = [];

  if (layerNumber == null || layerNumber == undefined) {
    for (let layerModel of serviceModel.layers) {
        let layer = _buildOneLayerDescriptor(serviceName, layerModel);
        desc.layers.push(layer);
        }
    }
  else {
    let layerModel = serviceModel.layers.find((l) => {
        return l.id == layerNumber;
    });
    let layer = _buildOneLayerDescriptor(serviceName, layerModel);
    desc.layers.push(layer);
  }
  return desc;
}

function _buildOneLayerDescriptor(serviceName, layerModel) {
    if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["layerModel:", layerModel.id]));
    const layer = {
    metadata: {
        maxRecordCount: MAX_RECORD_COUNT
    }
    };

    // copy all the properties from the layer model
    for (let propName in layerModel) {
    layer.metadata[propName] = layerModel[propName];
    }
    let layerModelIndexEntry = getLayerModelIndexEntry(serviceName, layerModel.id);
    layer.metadata.fields = layerModelIndexEntry.columnDefs;

//    setFieldDescriptors(layerModel, serviceModel);

    // add the list of fields to the metadata
//   layer.metadata.fields = layerModel.gdsMetadata.columnDefs;
    return layer;
}

exports.getServiceModels = module.amp(getServiceModels);
exports.getServiceModel = module.amp(getServiceModel);
exports.getLayerModel = module.amp(getLayerModel);
exports.saveServiceModel = module.amp(saveServiceModel);
exports.generateLayerDescriptor = module.amp(generateLayerDescriptor);
exports.generateServiceDescriptor = module.amp(generateServiceDescriptor);
exports.getColumnDefs = module.amp(getColumnDefs);