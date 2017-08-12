from flask import jsonify, make_response, request, abort, Blueprint
from werkzeug.security import generate_password_hash, check_password_hash

from app import mongodb
from app.exceptions import UserExistsException


auth = Blueprint('auth', __name__, url_prefix='/auth')

db = mongodb.get_db()
users_collection = db['users']


@auth.route('/register', methods=['POST'])
def register_user():
    request_json = request.get_json()
    username = request_json.get('username')
    email = request.json.get('email')
    password = request_json.get('password')
    if username is None or password is None or email is None:  # todo: delete, this should be done on client side
        abort(400)
    if users_collection.find_one({'username': username}) is not None:
        raise UserExistsException(username)

    password_hash = generate_password_hash(password)
    users_collection.insert({
        'username': username,
        'email': email,
        'password_hash': password_hash
    })
    return make_response(jsonify({'registered': True}), 201)


@auth.route('/login', methods=['POST'])
def login():
    request_json = request.get_json()
    username = request_json.get('username')
    password = request_json.get('password')

    user = users_collection.find_one({'username': username})
    if not user:
        abort(400)

    success = check_password_hash(user['password_hash'], password)
    return make_response(jsonify({'logged_in': success}), 200)