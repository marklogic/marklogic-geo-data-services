
'use strict';

const gslib = require('/lib/geoserverLib.sjs');
const geoserverUrl = '%%geoserverUrl%%'

function post(context, params, input) {
    xdmp.trace("GEOSERVER-DEBUG", "Starting post");
    xdmp.trace("GEOSERVER_DEBUG", geoserverUrl);

    const auth = {
        "method":"basic",
        "username":params.geoserverUser,
        "password":params.geoserverPassword
    }

    try {
        return gslib.geoserverPublisher(
            geoserverUrl,
            params.geoserverWorkspace,
            params.geoserverDatastore,
            auth,
            params.serviceDescriptorUri
        )
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

