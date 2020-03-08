'use strict';

const STATUS_CODE_400 = "Bad Request";
const STATUS_CODE_404 = "Not Found";
const STATUS_CODE_422 = "Unprocessable Entity";
const STATUS_CODE_500 = "Internal Server Error";

class ServiceError {
  constructor(message, statusCode, statusMsg) {
    this.context = {
      message: message,
      statusCode: statusCode,
      statusMsg: statusMsg
    };
  }

  get message() {
    return this.context.message;
  }

  get statusCode() {
    return this.context.statusCode;
  }

  get statusMsg() {
    return this.context.statusMsg;
  }
};

class NotFoundError extends ServiceError {
  constructor(message) {
    super(message, 404, STATUS_CODE_404);
  }
}

class InputError extends ServiceError {
  constructor(message) {
    super(message, 422, STATUS_CODE_422);
  }
}

class InternalError extends ServiceError {
  constructor(message) {
    super(message, 500, STATUS_CODE_500);
  }
}

function handleError(err) {
  var errData = null;
  if (err instanceof ServiceError) {
    errData = [ err.statusCode, err.statusMsg, err.message ];
  }
  else if (err instanceof Error) {
    errData = [ 500, STATUS_CODE_500, err.toString() ];
  }
  else {
    errData = [ 500, STATUS_CODE_500, JSON.stringify(err) ];
  }
  fn.error(null, "RESTAPI-SRVEXERR", Sequence.from(errData));
}


exports.handleError = handleError;
exports.newNotFoundError = (msg) => new NotFoundError(msg);
exports.newInternalError = (msg) => new InternalError(msg);
exports.newInputError = (msg) => new InputError(msg);
