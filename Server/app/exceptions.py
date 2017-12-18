class ErrorException(Exception):
    status_code = 400
    message = 'UNKNOWN_ERROR'

    def __init__(self, payload=None):
        super().__init__()
        self.payload = payload

    def to_dict(self):
        result = dict(self.payload or ())
        result['message'] = self.message
        return result


class UserExistsException(ErrorException):
    message = 'LOGIN_ALREADY_USED'


class EmailTakenException(ErrorException):
    message = 'EMAIL_ALREADY_USED'


class InvalidUsernameException(ErrorException):
    message = 'LOGIN_DOES_NOT_EXIST'


class InvalidEmailException(ErrorException):
    message = 'EMAIL_DOES_NOT_EXIST'


class InvalidPasswordException(ErrorException):
    message = 'LOGIN_PASSWORD_INVALID'
    status_code = 401


class JSONMissingException(ErrorException):
    message = 'JSON_MISSING'


class JSONListMissingException(ErrorException):
    message = "JSON_LIST_MISSING"


class InvalidFieldException(ErrorException):
    message = 'INVALID_FIELD'


class InsufficientPrivilegesException(ErrorException):
    message = 'INSUFFICIENT_PRIVILEGES'
    status_code = 403
