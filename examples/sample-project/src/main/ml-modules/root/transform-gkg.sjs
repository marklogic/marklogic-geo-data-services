'use strict';

function transformGKG(content, context) {
  let valueObj = content.value.toObject();

  // add another set of coordinates to allow for a geo element pair index
  valueObj.geometry.latlongs = {
    lon: valueObj.geometry.coordinates[0],
    lat: valueObj.geometry.coordinates[1]
  };

  content.value = xdmp.unquote(xdmp.quote(valueObj));
  return content;
}

exports.transformGKG = transformGKG;