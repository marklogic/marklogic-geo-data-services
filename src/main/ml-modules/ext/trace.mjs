const EVENT_DEBUG = 'GDS-DEBUG';

function traceDebug(msg) {
  xdmp.trace(EVENT_DEBUG, msg);
}

export { traceDebug };