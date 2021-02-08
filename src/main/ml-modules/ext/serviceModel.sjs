'use strict';

/**
 * The idea behind this module is to make retrieving the service and layer model(s)
 * as efficient as possible, and to retrieve them only once per transaction.  Also,
 * we need to avoid the use of server fields for the layer models themselves.  
 */

const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');
const sfc = require('/ext/server-field-cache.xqy');

const SERVICE_DESCRIPTOR_COLLECTION = 'http://marklogic.com/feature-services';
const MAX_RECORD_COUNT = 5000;

const DEBUG = xdmp.traceEnabled("GDS-DEBUG");

/**
 * This is a transaction-level index to hold service/layer models.  We reference
 * the layer model several times during a transaction and this allows us easy/fast
 * access to it.
 */
var serviceModelIndex = {
  
};

/**
 * Retrieveing a layer model involves verifying that the underlying view beneath it
 * has not changed.  We do that by calculating a 64-bit hash of the output of tde.getView(). 
 * This object gives us a fast transaction-level index for the hashes associated with
 * each view in the service/layer models.
 */
var viewHashIndex = {
  
}

/**
 * 
 * @param filter can be set to 'all', 'search' or 'geoserver'.alert
 * 
 * This method returns all service models that match the submitted filter.
 */
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

/**
 * returns the service model for the given service Id.
 * @param serviceId {String} The service id.
 */
function getServiceModel(serviceId) {
    let entry = getServiceModelIndexEntry(serviceId, false);
    return entry.serviceModel;
}

function setLayerModelIndexEntry(serviceId, layer) {
    let entry = getServiceModelIndexEntry(serviceId);
    entry.layerModelIndex[layer.id] = layer;
}

/**
 * This function validates that the view beneath the layerModel has not changed.  It
 * does this by looking at the cached 64-bit hash(es) of the schema.view(s) the layer
 * is based on and comparing those to the 64-bit hash(es) of the current version of
 * the view(s) in the database.
 * @param layerModelEntry {Object} The layer model to be validated.
 */
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
          if (DEBUG) xdmp.trace("GDS-DEBUG", "comparing layerModel view Hash " + descriptorLayerViewHashes[hash] + " to dbLayerView Hash " + dbLayerViewHashes[hash]);
            return descriptorLayerViewHashes[hash] != dbLayerViewHashes[hash]
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

/**
 * Return the layerModel index entry as a 'rehydrated' JSON object.  This object is
 * stored as stringified JSON in a document metadata field on the service descriptor
 * document.  The layerModel is created by parsing the contents of the document metadata field,
 * validating the view hashes, and if validation passes it is cached in the corresponding
 * serviceModel index entry and returned.  If either the metadata field is empty or the 
 * view hashes fail validation, null is returned from this function.  This will force the layerModel
 * to be regenerated and stored back to the db.
 * @param serviceModelEntry {Object} the service model index entry from the transaction index
 * @param layerId {int} the layer id we are looking for.
 */
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

/**
 * This function (re)generates a layerModelIndexEntry for the given serviceModel
 * and layer id. 
 * @param serviceModel {Object} the service model
 * @param layerId {int} the layer id
 */
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

/**
 * Convenience function to efficiently retrieve the column definitions from
 * index/cache as part of the query.
 * @param serviceName {String} the service name/id
 * @param layerId {int} the layer number
 */
function getColumnDefs(serviceName, layerId) {
  let layerModelIndexEntry = getLayerModelIndexEntry(serviceName, layerId);
  return layerModelIndexEntry.columnDefs;
}

/**
 * Stores the layer model index entries to the correct document metadata field in the database.
 * @param uri {String} the service descriptor document uri.
 * @param layerModelIndexEntry {Object} the layerModel index entry to be stored to the db
 */
function saveLayerModelIndexEntryInDb(uri, layerModelIndexEntry) {
    xdmp.invokeFunction(function() {
        declareUpdate();
        let metadata = xdmp.documentGetMetadata(uri) || {};
        metadata["layer_" + layerModelIndexEntry.layerId + "_model"] = layerModelIndexEntry;
        xdmp.documentSetMetadata(uri, metadata);
    },
    {isolation:"different-transaction"});
}

/**
 * Retrieves a layer model index entry from either the transaction cache or, if
 * we haven't already retrieved it, from the database.  If there is no index
 * entry saved in the database, it calculates a new one, caches it, stores it
 * in the database, and returns it.
 * @param serviceName {String} The service name/id 
 * @param layerId {int} the layer number.
 */
function getLayerModelIndexEntry(serviceName, layerId) {
    let serviceModelEntry = null;
    try {
        //get the service model, all we need here is a shallow copy/shell of the service model
        serviceModelEntry = getServiceModelIndexEntry(serviceName);
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
            let serviceModel = getServiceModel(serviceName);
            layerModelEntry = calculateNewLayerModelIndex(serviceModel, layerId);
            //save it in the db
            saveLayerModelIndexEntryInDb(serviceModelEntry.uri, layerModelEntry);
            //set it in the transaction cache
            serviceModelEntry.layerModelIndex[layerId] = layerModelEntry;
            return layerModelEntry;
        }
    }
}

/**
 * Gets a service model from the local transaction index.  It will return the 
 * full service model or a lightweight shell, depending on the value of the 'lightweight' 
 * parameter.
 * @param serviceId {String} The service name/id we are looking for
 * @param lightweight {boolean} @default true.  This is a boolean value that determines whether 
 * we pull the full service descriptor from disk and build out a full service model entry 
 * for all layers or whether we create a skeleton/shell entry that can be filled out later.
 * Usually we want to do the latter.
 */
function getServiceModelIndexEntry(serviceId, lightweight=true) {
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
    if ((lightweight) || (!lightweight && entry.serviceModel != null))
      return entry;
  }

  //If we got here, there's no index entry for the service model so we either pull it via a search
  //or create a skeleton entry for it, depending on the value of the 'lightweight' param.  Either way
  //we need this query.
  let modelQuery = cts.andQuery([
    cts.collectionQuery(SERVICE_DESCRIPTOR_COLLECTION),
    cts.jsonPropertyValueQuery("name", serviceId, ["exact"])
  ]);

  if (!lightweight) {
    //if lightweight==false, we always run a cts:search.  If we recently ran a search for the doc, it'll
    //still be in cache and is just as fast as a server field, and if the doc changed on disk
    //we'll always get the latest copy this way.
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
    
    // if lightweight == true, we just make a shell entry to hold the layer index.  This
    //avoids the overhead of pulling the full descriptor doc.
    let uri = fn.head(cts.uris(null, null, modelQuery));
    if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): serviceModelIndex[serviceId]: uri = " + uri);
    setServiceModelIndexEntry(null, uri, serviceId);
  }
  if (DEBUG) xdmp.trace("GDS-DEBUG", "getServiceModelIndexEntry(): returning index entry");
  return serviceModelIndex[serviceId];
}

/**
 * This function wraps a service descriptor doc in an index entry.  If modelDoc is null, it will use
 * the uri and serviceName params to create a lightweight shell entry.
 * 
 * @param modelDoc {Node} the service model document from disk, may be null if uri and service name are provided
 * @param uri {String} @default [null] The uri of the service model document. Used when creating a lightweight entry.    
 * @param serviceName {String} @default [null] The service's service name/id. Used when creating a lightweight Entry.
 */
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

/**
 * Returns the layer model for the given service name/id and layer id.
 * @param serviceName {String} the service name/id
 * @param layerId {int} the layer number.
 */
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

/**
 * Saves a new service model to the database.
 * @param serviceId {String} the service name/id
 * @param model {Object} the service model
 * @param uri {String} the uri to save the service descriptor to.
 */
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

/**
 * Retreive a view hash from the server field cache.  The key should be of the following
 * form:  "[schema].[view name]".  This relies on the server-field-cache.xqy code that
 * allows us to store items in the server field for a specific amount of time and then
 * they get booted out.  This lets us refresh the view hashes every so often.
 * 
 * @param viewNameKey {String} key for the view hash in the form "schemaName.viewName"
 */
function getCachedViewHash(viewNameKey) {
    let cachedViewHash = fn.head(sfc.get(viewNameKey));
    //if it's in the serer field cache, use that
    if (cachedViewHash) {
        if (DEBUG) xdmp.trace("GDS-DEBUG", "Found viewHash in server field cache, " + viewNameKey + ": " + cachedViewHash);
        viewHashIndex[viewNameKey] = cachedViewHash;
    }
    return cachedViewHash;
}

/**
 * Return cached view hashes.  In the case where we don't have a cached value for it, we will
 * add the schema.view name to the viewsNeedingHashesCalculated parameter so we can calculate all the view hashes we
 * need in one call.
 * 
 * @param viewNameKey {String} key for the view hash in the form "schemaName.viewName"/
 * @param viewHashes {Object} an object to hold the view hashes
 * @param viewsNeedingHashesCalculated {Array} array that will hold any schema.view combinations that need a 
 * hash calculated
 */
function getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewsNeedingHashesCalculated) {
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
            viewsNeedingHashesCalculated.push(viewNameKey);
        }
    }
}

/**
 * 
 * @param layerModelIndexEntry the layer index entry.
 * This function returns the locally cached
 * current hashes for the views referenced by the layer.  We will
 * evaluate whether they match the layer's pre-calculated hashes in
 * the viewUpdated() function.
 */
function getViewHashes(layerModelIndexEntry) {
    let layer = layerModelIndexEntry.layerModel;
    let viewsNeedingHashesCalculated = [];
    let viewHashes = {};

    //If the layerModel index entry already has hashes pre-calculated, we'll
    //look at those and return them
    if (layerModelIndexEntry.viewHashes != null && Object.keys(layerModelIndexEntry.viewHashes).length > 0) {
        for (let viewNameKey in layerModelIndexEntry.viewHashes) {
            getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewsNeedingHashesCalculated);
        }
    }
    //if there are no viewHashes, we have to look to see if there
    //*should* be viewHashes
    else {
        if (layer.source == "view" || layer.dataSources == null) {
            let viewNameKey = layer.schema + "." + layer.view;
            getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewsNeedingHashesCalculated);
        }
        if (layer.dataSources && Array.isArray(layer.dataSources)) {
          for (let ds of layer.dataSources) {
              if (ds.source == "view") {
                  let viewNameKey = ds.schema + "." + ds.view;
                  getCachedHashOrDeferCalculation(viewNameKey, viewHashes, viewsNeedingHashesCalculated);
              }
          }
        }
    }

    if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["viewsNeedingHashesCalculated:", viewsNeedingHashesCalculated, "viewHashes:", viewHashes]));
    if (viewsNeedingHashesCalculated.length > 0) {
        //only go calculate hashes if we haven't done so already in this transaction
        if (DEBUG) xdmp.trace("GDS-DEBUG", Sequence.from(["calculating view hashes", viewsNeedingHashesCalculated]));

        //We calculate these in xquery because it is WAY faster than javascript
        let retrievedViewHashes = fn.head(xdmp.invoke("/ext/view-hash.xqy", { input: Sequence.from(viewsNeedingHashesCalculated) }));
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


/**
 * This function determines if the view has been updated since the layer model index entry
 * was rendered.
 * @param layerModelIndexEntry {Object} the layer model index entry to check
 */
function viewUpdated(layerModelIndexEntry) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting viewUpdated");

  //if we have no view hashes then we always need to check
  if (layerModelIndexEntry.viewHashes == null)
    return true;
  
  //get the hashes for the view(s) this layer is based on
  let viewHashes = getViewHashes(layerModelIndexEntry);
  if (Object.keys(viewHashes).length != Object.keys(layerModelIndexEntry.viewHashes).length) {
    if (xdmp.traceEnabled("GDS-DEBUG")) {
      if (DEBUG) xdmp.trace("GDS-DEBUG", "computed viewhashes length:");
      if (DEBUG) xdmp.trace("GDS-DEBUG", Object.keys(viewHashes).length);
      if (DEBUG) xdmp.trace("GDS-DEBUG", "layer viewhashes length:");
      if (DEBUG) xdmp.trace("GDS-DEBUG", Object.keys(layerModelIndexEntry.viewHashes).length);
    }
    //if the lengths of the layer's view hashes and the introspected view hashes are different, return true
    return true;
  }
  for (let key in viewHashes) {
    if (layerModelIndexEntry.viewHashes[key] == null || !viewHashes[key].equals(layerModelIndexEntry.viewHashes[key])) {
      if (xdmp.traceEnabled("GDS-DEBUG")) {
        xdmp.trace("GDS-DEBUG", "viewhash different");
        xdmp.trace("GDS-DEBUG", layerModelIndexEntry.viewHashes);
        xdmp.trace("GDS-DEBUG", viewHashes);
      }
      //if any of the view hashes are different, return true
      return true;
    }
  }
if (DEBUG) xdmp.trace("GDS-DEBUG", "layer views up-to-date");
return false;
}

/**
 * Returns the schema that the layer is based on.  If the descriptor/layer has no schema
 * or view specified, return the serviceName as a placeholder schema name.
 * @param layerDesc {Object} the layer descriptor
 * @param serviceName {String} the service name/id the layer belongs to
 */
function getSchema(layerDesc, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting getSchema");
  return layerDesc.schema || serviceName;
}

/**
 * Generates an array of field descriptors for the overall layer.  This will
 * combine any joined views to create a single array of column names and datatypes.
 * 
 * @param layerModel {Object} the layer model for the service layer
 * @param serviceName {String} the service name/id
 */
function generateFieldDescriptors(layerModel, serviceName) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateFieldDescriptors");
  if (layerModel.view === undefined) {
    return generateFieldDescriptorsFromDataSourcesArray(layerModel, serviceName);
  } else {
    return generateFieldDescriptorsFromViewAndJoins(layerModel, serviceName);
  }
}

/**
 * Create field descriptors for a layer that uses a view.
 * @param layerModel {Object} the layer model for the service layer
 * @param serviceName {String} the service name/id
 */
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

/**
 * Create field descriptors from a set of joined dataSources
 * @param layerModel {Object} the layer model for the service layer
 * @param serviceName {String} the service name/id
 */
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

/**
 * Create field descriptors from the join array
 * @param layerModel {Object} the layer model for the service layer
 */
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

/**
 * Create field descriptors from a single dataSource array element
 * @param dataSource {Object} a single dataSources array element from the layer's dataSources aray
 */
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

/**
 * Create field descriptors for a dataSource based on a view definition
 * @param viewDef {Object} a view definition returned by tde.getView()
 * @param dataSource {String} the service name/id
 */
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

/**
 * 
 * @param fieldName {String} the column name
 * @param scalarType {String} the colunn's data type
 * @param alias {String} the Esri alias that will be used for this field
 * @param includeFields Unused
 */
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

/**
 * Returns the Esri datatype for the supplied XML Schema datatype localname 
 * @param datatype the localname of the XML Schema datatype
 */
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

/**
 * Generates a layer descriptor for the supplied layer id.  This function should be used only to 
 * back the Esri catalog request for the layer descriptor itself.  It should not be used to return
 * the layer descriptor for data requests because it generates the full service descriptor and all 
 * layer descriptors beneath it, making it relatively inefficient.
 * 
 * @param serviceName {String} The service name/id
 * @param layerNumber {int} the layer number
 */
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

/**
 * Generates a service descriptor for the supplied service id.  This function should be used only to 
 * back the Esri catalog request for the service descriptor itself.  It should not be used to return
 * the service descriptor for data requests because it generates the full service descriptor and all 
 * layer descriptors beneath it, making it relatively inefficient.
 * 
 * @param serviceName {String} The service name/id
 * @param layerNumber {int} the layer number
 */
function generateServiceDescriptor(serviceName, layerNumber) {
  if (DEBUG) xdmp.trace("GDS-DEBUG", "Starting generateServiceDescriptor");

  let serviceModelIndexEntry = getServiceModelIndexEntry(serviceName, false);

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

/**
 * Transforms the on-disk service model document into a descriptor that can be returned
 * to the client for the catalog service calls.
 * 
 * @param serviceModel {Node} the service descriptor document from the db
 * @param serviceName {String} the service name/id
 * @param layerNumber {int} the layer number
 */
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

/**
 * Builds a descriptor that can be used to answer the Esri catalog service requests for
 * the layer definition.
 * @param serviceName {String} the service name/id
 * @param layerModel {Object} the layer model for the layer
 */
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