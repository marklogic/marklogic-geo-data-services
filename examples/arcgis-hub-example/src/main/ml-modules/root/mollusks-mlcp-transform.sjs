'use strict';
function transform(content, context) {
  const doc = content.value.toObject();
  doc.geometry = {
    "type": "Point",
    "coordinates": [parseFloat(doc.X), parseFloat(doc.Y)],
  };
  content.value = doc;
  return content;
}
exports.transform = transform;
