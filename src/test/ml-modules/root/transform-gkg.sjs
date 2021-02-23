'use strict';

function transformGKG(content, context) {
  let obj = content.value.toObject();

  // add another set of coordinates to allow for a geo element pair index;
  // only used to circumvent bug where v1/search has issues with geo path indexes
  const geometry = obj.geometry || (obj.feature && obj.feature.geometry);
  if (geometry) {
    geometry.latlongs = {
      lon: geometry.coordinates[0],
      lat: geometry.coordinates[1]
    };
  }

  content.value = xdmp.unquote(xdmp.quote(obj));
  return content;
}

exports.transformGKG = transformGKG;