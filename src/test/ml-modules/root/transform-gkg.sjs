'use strict';

function transformGKG(content, context) {
  let valueObj = content.value.toObject();

  // add another set of coordinates to allow for a geo element pair index;
  // only used to circumvent bug where v1/search has issues with geo path indexes
  valueObj.geometry.latlongs = {
    lon: valueObj.geometry.coordinates[0],
    lat: valueObj.geometry.coordinates[1]
  };

  content.value = xdmp.unquote(xdmp.quote(valueObj));
  return content;
}

exports.transformGKG = transformGKG;