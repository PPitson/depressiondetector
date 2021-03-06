import mongoengine as mongo
from flask import jsonify, request, Blueprint, render_template, flash, redirect, url_for

import app.exceptions as exceptions
from app.commons import get_json_or_raise_exception
from app.email import send_email
from app.forms import PasswordResetForm
from app.models import User

auth = Blueprint('auth', __name__, url_prefix='/auth')


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
    contact_person_email = request_json.get('contact_person_email')
    contact_person_phone = request_json.get('contact_person_phone')
    user = User(username=username, email=email, sex=sex, date_of_birth=date_of_birth,
                contact_person_email=contact_person_email, contact_person_phone=contact_person_phone)
    user.password = password
    user.save()
    return jsonify({'message': 'SIGNUP_USER_REGISTERED'}), 201


@auth.route('/login', methods=['POST'])
def login():
    request_json = get_json_or_raise_exception()
    email = request_json['email']
    password = request_json['password']

    try:
        user = User.objects.get(email=email)
    except mongo.DoesNotExist:
        raise exceptions.InvalidEmailException

    if not user.verify_password(password):
        raise exceptions.InvalidPasswordException

    result = {'message': 'LOGIN_USER_LOGGED_IN', 'user_data': user.to_json()}
    return jsonify(result), 200


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
