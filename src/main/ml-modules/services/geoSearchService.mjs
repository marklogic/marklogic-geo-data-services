import { handleError } from '/ext/error';
import { geoSearch } from '/ext/search/geoSearch';

function post(context, params, input) {
  try {
    return geoSearch(input);
  }
  catch (error) {
    handleError(error);
  }
}

exports.POST = post;