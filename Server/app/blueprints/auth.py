from flask import jsonify, request, abort, Blueprint, g
from werkzeug.security import generate_password_hash

from app.models import User
import mongoengine as mongo
from app.exceptions import UserExistsException, InvalidPasswordException, InvalidUsernameException
from app.http_auth import auth as http_basic_auth

auth = Blueprint('auth', __name__, url_prefix='/auth')


@auth.route('/register', methods=['POST'])
def register_user():
    request_json = request.get_json()
    username = request_json.get('username')
    email = request.json.get('email')
    password = request_json.get('password')
    if username is None or password is None or email is None:  # todo: delete, this should be done on client side
        abort(400)

    if User.objects.filter(username=username).first() is not None:
        raise UserExistsException(username)

    password_hash = generate_password_hash(password)
    sex = request_json.get('sex')
    date_of_birth = request_json.get('date_of_birth')
    user = User(username=username, email=email, password_hash=password_hash, sex=sex, date_of_birth=date_of_birth)
    user.save()
    return jsonify({'registered': True}), 201


@auth.route('/login', methods=['POST'])
def login():
    request_json = request.get_json()
    username = request_json.get('username')
    password = request_json.get('password')

    try:
        user = User.objects.get(username=username)
    except mongo.DoesNotExist:
        raise InvalidUsernameException(username)
    success = user.verify_password(password)
    if not success:
        raise InvalidPasswordException()
    return jsonify({'logged_in': True}), 200


@auth.route('/change_password', methods=['POST'])
@http_basic_auth.login_required
def change_password():
    request_json = request.get_json()
    new_password = request_json.get('new_password')
    password_hash = generate_password_hash(new_password)
    g.current_user.password_hash = password_hash
    g.current_user.save()
    return jsonify({'changed_password': True}), 200


@auth.route('/change_email', methods=['POST'])
@http_basic_auth.login_required
def change_email():
    new_email = request.get_json().get('new_email')
    g.current_user.email = new_email
    g.current_user.save()
    return jsonify({'changed_email': True}), 200