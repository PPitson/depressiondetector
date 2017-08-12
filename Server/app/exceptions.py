class ErrorException(Exception):
    status_code = 400

    def __init__(self, message, status_code=None, payload=None):
        super().__init__()
        self.message = message
        if status_code is not None:
            self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        result = dict(self.payload or ())
        result['error'] = self.message
        return result


class UserExistsException(ErrorException):
    def __init__(self, username, payload=None):
        super().__init__(message=f'User {username} already exists', payload=payload)


class InvalidUsernameException(ErrorException):
    def __init__(self, username, payload=None):
        super().__init__(message=f'User {username} does not exist', payload=payload)


class InvalidPasswordException(ErrorException):
    def __init__(self, payload=None):
        super().__init__(message=f'Invalid password', status_code=401, payload=payload)

