'use strict';

function transform(content, context) {
  let doc = content.value.toObject();
  // Add the required geometry object so that the geospatial query constructed by GDS finds the expected data.
  doc.geometry = {
    "type": "Point",
    "coordinates": [parseFloat(doc.X), parseFloat(doc.Y)],
  };
  content.value = doc;
  return content;
}

exports.transform = transform;
