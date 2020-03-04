import { ServiceModel } from 'models';
import { InputError } from '../error';
const search = require('/MarkLogic/appservices/search/search.xqy');
const sut = require('/MarkLogic/rest-api/lib/search-util.xqy');

/*
  Sample payload:
  {
    "params": {
      "id": "service descriptor name",  // Required; determines available search profiles
      "search": "search profile name",  // Required; extract search options from specified profile
      "request": [ "results", "facets", "values", "suggest" ],  // Optional; defaults to: results, facets, values
      "aggregateValues": true // Optional; defaults to true
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
function withDefaults(input)
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
    params: { id, search, request, aggregateValues, ...paramsRest },
    search: { qtext, start, pageLength, facets, viewport: { box, latDivs, lonDivs, zoom }, queries, ...searchRest },
    ...inputRest
  };
  
  // fill these out if null
  newInput.params.request = newInput.params.request || [];
  newInput.search.facets = newInput.search.facets || {};
  newInput.search.queries = newInput.search.queries || {};
  
  // ensure these are arrays (if single values were provided)
  if (!Array.isArray(newInput.params.request)) { newInput.params.request = [ newInput.params.request ]; }
  
  return newInput;
}

function constructSearchDocs(searchProfile, input) {
  // get stored options from modules
  const baseOptions = sut.options({ options: searchProfile.optionsName });
  
  // collect all structured queries to be injected into search:search
  const structuredQueries = [];
  structuredQueries.push(input.search.queries); // add any additional queries provided in input (request)
  
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
  
  // construct search:search "override"
  const deltaSearch = {
    "search": {
      "query": {
        "queries": structuredQueries
      },
      "options": {
        "page-length": input.search.pageLength,
        "constraint": []
      }
    }
  };
  
  // merge search:options
  const deltaSearchDoc = sut.searchFromJson(deltaSearch);
  const mergedOptionsDoc = sut.mergeOptions(baseOptions, deltaSearchDoc.xpath("search:options"));
  
  return {
    options: mergedOptionsDoc, 
    query: deltaSearchDoc.xpath("search:query")
  };
}

function getSearchResults(model, searchProfile, input) {
  const sdoc = constructSearchDocs(searchProfile, input);
  const response = sut.responseToJsonObject(search.resolve(sdoc.query, sdoc.options, input.search.start, input.search.pageLength), "all");
  return fn.head(response).toObject();
}

function getSearchSuggestions(model, searchProfile, input) {

}

function geoSearch(input) {
  const _input = withDefaults(input);
  
  if (!_input.params.id) { throw new InputError("No service descriptor name provided in the property params.id"); }
  const model = new ServiceModel(_input.params.id);
  if (!model.hasSearchProfiles) { throw new InputError(`The service descriptor \"${model.name}\" has no declared search profiles.`); }
  if (!_input.params.search) { throw new InputError("No search profile name provided in the property params.search"); }
  const searchProfile = model.getSearchProfile(_input.params.search);
  if (searchProfile === null) { throw new InputError(`The service descriptor \"${model.name}\" doesn't have a search profile named \"${_input.params.search}\".`); }
  
  // check request to determine what to return
  const returnSearchOptions = new Set(["results", "facets", "values"]);
  const returnSearch = _input.params.request.some((opt) => returnSearchOptions.has(opt));
  const returnRequest = _input.params.request.some((opt) => opt === "returnRequest");
  const returnSuggest = _input.params.request.some((opt) => opt === "suggest");
  
  // response follows the structure returned by search:search 
  let response = returnSearch ? getSearchResults(model, searchProfile, _input) : {};

  // add search suggestions if requested
  if (returnSuggest) { response.suggestions = getSearchSuggestions(model, searchProfile, _input); }

  // return final input object if requested
  if (returnRequest) { response.returnRequest = _input };

  return response;
}

export { geoSearch };