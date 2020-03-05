const models = require('/ext/search/models.sjs');
const err = require('/ext/error.sjs');
const gsu = require('/ext/search/geo-search-util.xqy');

/*
  Sample payload:
  {
    "params": {
      "id": "service descriptor name",  // Required; determines available search profiles
      "search": "search profile name",  // Required; extract search options from specified profile
      "request": [ "results", "facets", "values", "suggest" ],  // Optional; defaults to: results, facets, values
      "aggregateValues": true, // Optional; defaults to true
      "valuesLimit": 1000, // Optional; defaults to 1000
      "debug": false // Optional; defaults to false
    },
    "search": {
      "qtext": "query text", // Optional; defaults to empty string
      "start": 1,            // Optional; defaults to 1
      "pageLength": 10,      // Optional; defaults to 10
      "facets": {            // Optional; defaults to empty object {}
        "constrantName1": [ 'value1', 'value2', ... ] // Optional; defaults to empty array []
        "constrantName2": [ 'value3', 'value4', ... ]
      }
      "viewport": {
        "box": { // Optional; defaults to cover entire coordinate system (90, -90, -180, 180)
          "n": 90.0, 
          "s": -90.0, 
          "w": -180.0,
          "e": 180.0
        },
        "latDivs": 100, // Optional; defaults to 100
        "lonDivs": 100, // Optional; defaults to 100
        "zoom": 10      // Optional; defaults to 10
      },
      "queries": {} // Optional; additional structured queries, defaults to empty object {}
    }    
  }
*/

/**
 * Fills in any gaps in input with default values and returns a new input object.
 * @param {Object} input A geo search input object (the POST request's body)
 */
function resolveInput(input)
{
  const defaultDivs = 100;
  
  // default input object structure
  var _input = {};
  _input.params = input.params || {};
  _input.search = input.search || { viewport: {} };
  _input.search.viewport = _input.search.viewport || {};
  
  // extract values from input and set default values if a property is missing
  let {
    params: {
      id: id,
      search: search,
      request: request = [ 'results', 'facets', 'values' ],
      aggregateValues: aggregateValues = true,
      valuesLimit: valuesLimit = 1000,
      debug: debug = true,
      ...paramsRest // pass along any extra properties
    },
    search: {
      qtext: qtext = "",
      start: start = 1,
      pageLength: pageLength = 10,
      facets: facets = null,
      viewport: {
        box: box = { n: 90.0, s: -90.0, w: -180.0, e: 180.0 },
        latDivs: latDivs = defaultDivs,
        lonDivs: lonDivs = defaultDivs,
        zoom: zoom = 10
      },
      queries: queries = null,
      ... searchRest // pass along any extra properties
    },
    ...inputRest
  } = _input;
  
  // create new input object
  let newInput = {
    params: { id, search, request, aggregateValues, valuesLimit, debug, ...paramsRest },
    search: { qtext, start, pageLength, facets, viewport: { box, latDivs, lonDivs, zoom }, queries, ...searchRest },
    ...inputRest
  };
  
  // fill these out if null
  newInput.params.request = newInput.params.request || [];
  newInput.search.facets = newInput.search.facets || {};
  newInput.search.queries = newInput.search.queries || [];
  
  // ensure these are arrays (if single values were provided)
  if (!Array.isArray(newInput.params.request)) { newInput.params.request = [ newInput.params.request ]; }
  
  return newInput;
}

function createSearchCriteria(searchProfile, input, returnResults, returnFacets, returnValues, debugMode) {
  // collect all structured queries to be injected into search:search
  const structuredQueries = [];
  
  // add qtext
  if (input.search.qtext) {
    structuredQueries.push({
      "term-query": {
        "text": [ input.search.qtext ]
      }
    });
  }
  
  // constrain search against current viewport
  const viewport = input.search.viewport;
  structuredQueries.push({
    "geospatial-constraint-query": {
      "constraint-name": searchProfile.geoConstraintName,
      "box": [{ "south": viewport.box.s, "west": viewport.box.w, "north": viewport.box.n, "east": viewport.box.e }]
    }
  });

  // add any additional queries provided in input (request)
  structuredQueries.push(input.search.queries);
  
  // create delta search:search
  const deltaSearchObj = {
    "search": {
      "query": {
        "queries": structuredQueries
      },
      "options": {
        "page-length": input.search.pageLength,
        "return-results": returnResults === true,
        "return-facets": returnFacets === true
      }
    }
  };
  const deltaSearch = gsu.searchFromJson(deltaSearchObj);

  return fn.head(gsu.createSearchCriteria(
    searchProfile.optionsName, 
    deltaSearch, 
    searchProfile.geoConstraintName,
    {
      "s": viewport.box.s, "w": viewport.box.w, "n": viewport.box.n, "e": viewport.box.e,
      "latdivs": viewport.latDivs, "londivs": viewport.lonDivs
    }));
}

function getSearchResults(model, searchProfile, input, returnResults, returnFacets, returnValues, debugMode) {
  // get search:search
  const criteria = createSearchCriteria(searchProfile, input, returnResults, returnFacets, returnValues);
  
  // get results
  const response = fn.head(gsu.getSearchResults(criteria, searchProfile.geoConstraintName, {
    start : input.search.start,
    pageLength: input.search.pageLength,
    aggregateValues: input.params.aggregateValues
  })).toObject();

  if (debugMode) {
    response.debug = {
      ...response.debug,
      criteria: gsu.searchToJson(criteria) // expose search:search
    };
  }
  
  return response;
}

function getSearchSuggestions(model, searchProfile, input, debugMode) {

}

function geoSearch(input) {
  const _input = resolveInput(input);
  
  if (!_input.params.id) { throw err.newInputError("No service descriptor name provided in the property params.id"); }
  const model = models.loadServiceModel(_input.params.id);
  if (!model.hasSearchProfiles) { throw err.newInputError(`The service descriptor \"${model.name}\" has no declared search profiles.`); }
  if (!_input.params.search) { throw err.newInputError("No search profile name provided in the property params.search"); }
  const searchProfile = model.getSearchProfile(_input.params.search);
  if (searchProfile === null) { throw err.newInputError(`The service descriptor \"${model.name}\" doesn't have a search profile named \"${_input.params.search}\".`); }
  
  // check what to return
  const returnResults = _input.params.request.some((opt) => opt === "results");
  const returnFacets = _input.params.request.some((opt) => opt === "facets");
  const returnValues = _input.params.request.some((opt) => opt === "values");
  const returnSuggest = _input.params.request.some((opt) => opt === "suggest");
  const returnSearch = returnResults || returnFacets || returnValues;
  const debugMode = _input.params.debug === true;
  
  // response follows the structure returned by search:search 
  let response = returnSearch ? getSearchResults(model, searchProfile, _input, returnResults, returnFacets, returnValues, debugMode) : {};

  // add search suggestions if requested
  if (returnSuggest) { response.suggestions = getSearchSuggestions(model, searchProfile, _input, debugMode); }

  if (debugMode) {
    response.debug = {
      ...response.debug,
      resolvedInput: _input
    };
  }

  return response;
}

exports.geoSearch = geoSearch;