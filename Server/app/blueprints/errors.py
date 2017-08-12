from flask import make_response, jsonify, Blueprint

errors = Blueprint('errors', __name__)


@errors.app_errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)