
'use strict';

const gslib = require('/lib/geoserverLib.sjs');
const geoserverProfile = xdmp.fromJsonString('%%geoserverProfile%%')

function post(context, params, input) {
    xdmp.trace("GEOSERVER-DEBUG", "Starting post");

    try {
        return gslib.geoserverPublisher(geoserverProfile,params.serviceDescriptorUri)
    } catch (err) {
        console.log(err.stack);
        console.trace(err);
        returnErrToClient(500, 'Error handling request', err.toString());
    }
}
function returnErrToClient(statusCode, statusMsg, body) {
    xdmp.trace("GEOSERVER-DEBUG", "Starting returnErrToClient");
    fn.error(
        null,
        'RESTAPI-SRVEXERR',
        Sequence.from([statusCode, statusMsg, body])
    );
    // unreachable - control does not return from fn.error.
};

exports.POST = post;

