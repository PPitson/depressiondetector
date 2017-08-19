from flask import jsonify, Blueprint
from app.exceptions import ErrorException

errors = Blueprint('errors', __name__)


@errors.app_errorhandler(404)
def not_found(error):
    return jsonify({'error': 'Not found'}), 404


@errors.app_errorhandler(ErrorException)
def handle_error_exception(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response
