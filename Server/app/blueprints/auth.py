from flask import jsonify, request, Blueprint, g, render_template, redirect, flash, url_for
from werkzeug.security import generate_password_hash

from app.models import User
import mongoengine as mongo
import app.exceptions as exceptions
from app.http_auth import auth as http_basic_auth
from app.email import send_email
from app.forms import PasswordResetForm

auth = Blueprint('auth', __name__, url_prefix='/auth')


@auth.route('/register', methods=['POST'])
def register_user():
    request_json = request.get_json()
    username = request_json.get('username')
    email = request.json.get('email')
    password = request_json.get('password')

    if User.objects.filter(username=username).first() is not None:
        raise exceptions.UserExistsException(username)
    if User.objects.filter(email=email).first() is not None:
        raise exceptions.EmailTakenException(email)

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
        raise exceptions.InvalidUsernameException(username)

    if not user.verify_password(password):
        raise exceptions.InvalidPasswordException()

    return jsonify({'logged_in': True}), 200


@auth.route('/reset_password', methods=['POST'])
def reset_password_request():
    email = request.get_json().get('email')
    try:
        user = User.objects.get(email=email)
    except mongo.DoesNotExist:
        raise exceptions.InvalidEmailException(email)

    token = user.generate_reset_token()
    send_email(user.email, 'Reset your password', 'mail/reset_password', user=user, token=token)
    return jsonify({'sent_email': True}), 200


@auth.route('/reset_password/<token>', methods=['GET', 'POST'])
def reset_password(token):
    form = PasswordResetForm()
    if form.validate_on_submit():
        user = User.objects.filter(email=form.email.data).first()
        if user is not None and user.reset_password(token, form.password.data):
            flash('Your password has been updated.')
        else:
            flash('Failed to update your password')
        return redirect(url_for('auth.reset_password', token=token))
    return render_template('reset_password.html', form=form)


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
