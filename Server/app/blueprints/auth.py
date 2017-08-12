from flask import jsonify, make_response, request, abort, Blueprint
from werkzeug.security import generate_password_hash

from app import mongodb


auth = Blueprint('auth', __name__, url_prefix='/auth')
db = mongodb.get_db()
users_collection = db['users']


@auth.route('/register', methods=['POST'])
def register_user():
    request_json = request.get_json()
    username = request_json.get('username')
    password = request_json.get('password')
    if username is None or password is None:
        abort(400)
    if users_collection.find_one({'username': username}) is not None:
        abort(400)  # user already exists

    password_hash = generate_password_hash(password)
    users_collection.insert({
        'username': username,
        'password_hash': password_hash
    })
    return make_response(jsonify({'registered': True}), 201)