'use strict';

const EVENT_DEBUG = 'GDS-DEBUG';

function traceDebug(msg) {
  xdmp.trace(EVENT_DEBUG, msg);
}

exports.traceDebug = traceDebug;