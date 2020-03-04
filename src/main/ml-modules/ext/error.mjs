const STATUS_CODE_400 = "Bad Request";
const STATUS_CODE_422 = "Unprocessable Entity";
const STATUS_CODE_500 = "Internal Server Error";

class ServiceError {
  constructor(message, statusCode, statusMsg) {
    this.message = message;
    this.statusCode = statusCode;
    this.statusMsg = statusMsg;
  }

  get message() {
    return this.message;
  }

  get statusCode() {
    return this.statusCode;
  }

  get statusMsg() {
    return this.statusMsg;
  }
};

class InputError extends ServiceError {
  constructor(message) {
    super(message, 422, STATUS_CODE_422);
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
    errData = [ 500, STATUS_CODE_500, JSON.stringify(err) ]
  }
  fn.error(null, "RESTAPI-SRVEXERR", Sequence.from(errData));
}

export { ServiceError, InputError, handleError };