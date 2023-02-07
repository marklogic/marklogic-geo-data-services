
'use strict';

const gslib = require('/lib/geoserverLib.sjs');
// interpolated values from gradle properties won't work when GDS is deployed as a bundle
//const geoserverUrl = '%%geoserverUrl%%';
//const geoserverAuthType = '%%geoserverAuthType%%';

function post(context, params, input) {
  xdmp.trace("GEOSERVER-DEBUG", "Starting post");
  xdmp.trace("GEOSERVER-DEBUG", params.geoserverUrl);
  xdmp.trace("GEOSERVER-DEBUG", params.serviceDescriptorUri);

  const auth = {
    "method":params.geoserverAuthType,
    "username":params.geoserverUser,
    "password":params.geoserverPassword
  }

  try {
    return gslib.geoserverPublisher(
      params.geoserverUrl,
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

function deleteFunction(context, params) {
  xdmp.trace("GEOSERVER-DEBUG", "Starting delete");
  xdmp.trace("GEOSERVER-DEBUG", params.geoserverUrl);

  const auth = {
    "method":params.geoserverAuthType,
    "username":params.geoserverUser,
    "password":params.geoserverPassword
  }

  try {
    return gslib.geoserverUnpublisher(
      params.geoserverUrl,
      params.geoserverWorkspace,
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
exports.DELETE = deleteFunction;
