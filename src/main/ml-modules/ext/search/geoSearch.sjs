'use strict';
const sm = require('/ext/serviceModel.sjs');
const err = require('/ext/error.sjs');
const trace = require('/ext/trace.sjs');
const gsu = require('/ext/search/geo-search-util.xqy');

/*
  Sample payload:
  {
    "params": {
      "id": "service descriptor name",  // Required;
      "request": [ "results", "facets", "values", "suggest" ],  // Optional; defaults to: results, facets, values
      "aggregateValues": true, // Optional; defaults to true, only gets applied to "point" geometry
      "valuesLimit": 1000, // Optional; defaults to no limit
      "debug": false // Optional; defaults to false
    },
    "search": {
      "qtext": "query text", // Optional; defaults to empty string
      "start": 1,            // Optional; defaults to 1
      "pageLength": 10,      // Optional; defaults to 10
      "facets": {            // Optional; defaults to empty object {}
        "constrantName1": [ 'value' ] // Optional; defaults to empty array []
        "constrantName2": [ 'value' ]
      }
      "viewport": {
        "box": { // Optional; defaults to cover entire coordinate system (90, -90, -180, 180)
          "n": 90.0, 
          "s": -90.0, 
          "w": -180.0,
          "e": 180.0
        },
        "maxLatDivs": 100, // Optional; defaults to 100
        "maxLonDivs": 100, // Optional; defaults to 100
      },
      "queries": {} // Optional; additional structured queries, defaults to empty object {}
    }    
  }
*/

const DEFAULT_MIN_DIVS = 10;
const DEFAULT_MAX_DIVS = 100;

/**
 * Fills in any gaps in input with default values and returns a new input object.
 * @param {Object} input A geo search input object (the POST request's body)
 */
function resolveInput(input)
{
  const DEFAULT_MAX_DIVS = 100;
  
  // default input object structure
  var _input = {};
  _input.params = input.params || {};
  _input.search = input.search || { viewport: {} };
  _input.search.viewport = _input.search.viewport || {};
  
  // extract values from input and set default values if a property is missing
  let {
    params: {
      id: id,
      request: request = [ 'results', 'facets', 'values' ],
      aggregateValues: aggregateValues = true,
      valuesLimit: valuesLimit = 0,
      debug: debug = false,
      ...paramsRest // pass along any extra properties
    },
    search: {
      qtext: qtext = "",
      start: start = 1,
      pageLength: pageLength = 10,
      facets: facets = null,
      viewport: {
        box: box = { n: 90.0, s: -90.0, w: -180.0, e: 180.0 },
        maxLatDivs: maxLatDivs = DEFAULT_MAX_DIVS,
        maxLonDivs: maxLonDivs = DEFAULT_MAX_DIVS
      },
      queries: queries = null,
      ... searchRest // pass along any extra properties
    },
    ...inputRest
  } = _input;

  // set limits
  maxLatDivs = fn.min([DEFAULT_MAX_DIVS, fn.max([DEFAULT_MIN_DIVS, maxLatDivs])]);
  maxLonDivs = fn.min([DEFAULT_MAX_DIVS, fn.max([DEFAULT_MIN_DIVS, maxLonDivs])]);
  
  // create new input object
  let newInput = {
    params: { id, request, aggregateValues, valuesLimit, debug, ...paramsRest },
    search: { qtext, start, pageLength, facets, viewport: { box, maxLatDivs, maxLonDivs }, queries, ...searchRest },
    ...inputRest
  };
  
  // fill these out if null
  newInput.params.request = newInput.params.request || [];
  newInput.search.qtext = newInput.search.qtext || "";
  newInput.search.facets = newInput.search.facets || {};
  newInput.search.queries = newInput.search.queries || [];
  
  // ensure these are arrays (if single values were provided)
  if (!Array.isArray(newInput.params.request)) { newInput.params.request = [ newInput.params.request ]; }
  
  return newInput;
}

function getGeoConstraintNames(model) {
  return model.layers
    .filter(l => l.search && l.search.geoConstraint) // only layers with search constraint
    .map(l => l.search.geoConstraint)
    .filter((constraint, idx, self) => self.indexOf(constraint) === idx); // only unique constraint names
}

function createQueryText(input) {
  const terms = new Set();
  if (input.search.qtext.trim()) {
    terms.add(input.search.qtext);
  }
  if (input.search.facets) {
    for (let [key, values] of Object.entries(input.search.facets)) {
      const name = key.trim();
      if (Array.isArray(values) && name) { 
        values.forEach(v => {
          const value = v.trim();
          if (value) {
            terms.add(`${name}:\"${value}\"`);
          }
        }); 
      }
    }
  }
  return terms.size > 0 ? fn.stringJoin(Sequence.from(terms), " ") : "";
}

function createSearchCriteria(model, input, returnResults, returnFacets, returnValues, debugMode) {
  // collect all structured queries to be injected into search:search
  const structuredQueries = [];
  
  // constrain search against current viewport
  const geoConstraintNames = getGeoConstraintNames(model);
  const viewport = input.search.viewport;
  geoConstraintNames.forEach(constraintName => 
    structuredQueries.push({
      "geospatial-constraint-query": {
        "constraint-name": constraintName,
        "box": [{ "south": viewport.box.s, "west": viewport.box.w, "north": viewport.box.n, "east": viewport.box.e }]
      }
    })
  );

  // add any additional queries provided in input (request)
  structuredQueries.push(input.search.queries);
  
  // create delta search:search
  const aggregateValues = input.params.aggregateValues;
  const deltaSearchObj = {
    "search": {
      "query": {
        "queries": structuredQueries
      },
      "options": {
        "page-length": input.search.pageLength,
        "return-results": returnResults,
        "return-facets": returnFacets || returnValues,
        "return-values": returnValues
      }
    }
  };
  const deltaSearch = gsu.searchFromJson(deltaSearchObj);

  return fn.head(gsu.createSearchCriteria(
    model.search.options,
    deltaSearch, 
    geoConstraintNames,
    {
      fullQueryText: createQueryText(input),
      returnValues: returnValues,
      aggregateValues: aggregateValues,
      valuesLimit: input.params.valuesLimit,
      viewport: viewport,
      defaultMinDivs: DEFAULT_MIN_DIVS,
      defaultMaxDivs: DEFAULT_MAX_DIVS
    }));
}

function getSearchResults(model, input, returnResults, returnFacets, returnValues, debugMode) {
  // get search:search
  const criteria = createSearchCriteria(model, input, returnResults, returnFacets, returnValues);
  
  // get results
  const geoConstraintNames = getGeoConstraintNames(model);
  const response = fn.head(gsu.getSearchResults(criteria, geoConstraintNames, {
    start : input.search.start,
    pageLength: input.search.pageLength,
    aggregateValues: input.params.aggregateValues,
    returnResults: returnResults,
    returnFacets: returnFacets,
    returnValues: returnValues
  })).toObject();

  if (debugMode) {
    response.debug = {
      ...response.debug,
      criteria: gsu.searchToJson(criteria) // expose search:search
    };
  }
  
  return response;
}

function getSearchSuggestions(model, input, debugMode) {

}

function geoSearch(input) {
  const _input = resolveInput(input);
  if (!_input.params.id) { throw err.newInputError("No service descriptor ID provided in the property params.id"); }

  const model = sm.getServiceModel(_input.params.id);
  if (!(model.search && model.search.options)) { throw err.newInputError(`The service descriptor \"${model.info.name}" is not configured for use with geoSearchService: missing search options.`); }
  if (getGeoConstraintNames(model).length <= 0) { trace.warn(`The service descriptor \"${model.info.name}\" has no layers with a geoConstraint.`, "geoSearch"); }

  // check what to return
  const returnResults = _input.params.request.some((opt) => opt === "results");
  const returnFacets = _input.params.request.some((opt) => opt === "facets");
  const returnValues = _input.params.request.some((opt) => opt === "values");
  const returnSuggest = _input.params.request.some((opt) => opt === "suggest");
  const returnSearch = returnResults || returnFacets || returnValues;
  const debugMode = _input.params.debug === true;
  
  // response follows the structure returned by search:search 
  let response = returnSearch ? getSearchResults(model, _input, returnResults, returnFacets, returnValues, debugMode) : {};

  // add search suggestions if requested
  if (returnSuggest) { response.suggestions = getSearchSuggestions(model, _input, debugMode); }

  if (debugMode) {
    response.debug = {
      ...response.debug,
      resolvedInput: _input
    };
  }

  return response;
}

exports.geoSearch = geoSearch;