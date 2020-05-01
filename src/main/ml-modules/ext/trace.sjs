'use strict';

const EVENT_INFO = 'GDS-INFO';
const EVENT_WARNING = 'GDS-WARN';
const EVENT_ERROR = 'GDS-ERR';

function trace(severity, msg, loc) {
  xdmp.trace(severity, (loc ? (loc + ':') : '') + msg);
}

exports.info = (msg, loc) => { trace(EVENT_INFO, msg, loc); }
exports.warn = (msg, loc) => { trace(EVENT_WARNING, msg, loc); }
exports.error = (msg, loc) => { trace(EVENT_ERROR, msg, loc); }