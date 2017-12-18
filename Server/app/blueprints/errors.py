import json

from flask import jsonify, Blueprint
from mongoengine import ValidationError

from app.exceptions import ErrorException

errors = Blueprint('errors', __name__)


@errors.app_errorhandler(404)
def not_found(_):
    return jsonify({'message': 'NOT_FOUND'}), 404


@errors.app_errorhandler(ErrorException)
def handle_error_exception(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response


@errors.app_errorhandler(KeyError)
def handle_missing_key(error):
    response = jsonify({'message': 'FIELD_REQUIRED', 'field': error.args[0]})
    response.status_code = 400
    return response


@errors.app_errorhandler(ValueError)
def handle_wrong_datetime(error):
    response = jsonify({'message': str(error)})
    response.status_code = 400
    return response


@errors.app_errorhandler(ValidationError)
def handle_validation_error(error):
    field_name, message = list(error.to_dict().items())[0]
    response = jsonify({'message': message, 'field': field_name})
    response.status_code = 400
    return response


@errors.app_errorhandler(json.JSONDecodeError)
def handle_invalid_json(_):
    response = jsonify({'message': 'INVALID_JSON'})
    response.status_code = 400
    return response
