from flask import jsonify, request, Blueprint, render_template, flash, redirect, url_for

from app.models import User
import mongoengine as mongo
import app.exceptions as exceptions
from app.forms import PasswordResetForm
from app.email import send_email


auth = Blueprint('auth', __name__, url_prefix='/auth')


def get_json_or_raise_exception():
    request_json = request.get_json()
    if not request_json:
        raise exceptions.JSONMissingException
    return request_json


@auth.route('/register', methods=['POST'])
def register_user():
    request_json = get_json_or_raise_exception()
    username = request_json['username']
    email = request.json['email']
    password = request_json['password']

    if User.objects.filter(username=username).first() is not None:
        raise exceptions.UserExistsException
    if User.objects.filter(email=email).first() is not None:
        raise exceptions.EmailTakenException

    sex = request_json.get('sex')
    date_of_birth = request_json.get('date_of_birth')
    user = User(username=username, email=email, sex=sex, date_of_birth=date_of_birth)
    user.password = password
    user.save()
    return jsonify({'message': 'SIGNUP_USER_REGISTERED'}), 201


@auth.route('/login', methods=['POST'])
def login():
    request_json = get_json_or_raise_exception()
    username = request_json['username']
    password = request_json['password']

    try:
        user = User.objects.get(username=username)
    except mongo.DoesNotExist:
        raise exceptions.InvalidUsernameException

    if not user.verify_password(password):
        raise exceptions.InvalidPasswordException

    return jsonify({'message': 'LOGIN_USER_LOGGED_IN'}), 200


@auth.route('/reset_password', methods=['POST'])
def reset_password_request():
    request_json = get_json_or_raise_exception()
    email = request_json['email']
    try:
        user = User.objects.get(email=email)
    except mongo.DoesNotExist:
        raise exceptions.InvalidEmailException

    token = user.generate_token()
    send_email(user.email, 'Reset your password', 'mail/reset_password', user=user, token=token)
    return jsonify({'sent_email': True}), 200


@auth.route('/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    form = PasswordResetForm()
    if form.validate_on_submit():
        user = User.load_user_from_token(token)
        if user is not None:
            user.password = form.password.data
            user.save()
            flash('Your password has been updated.')
        else:
            flash('Failed to update your password: token expired or was incorrect, or account was deleted')
        return redirect(url_for('main.index'))
    return render_template('reset_password.html', form=form)