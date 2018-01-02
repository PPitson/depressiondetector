from flask import Blueprint, g, jsonify, flash, redirect, url_for

from app.email import send_email
from app.http_auth import auth
from app.models import User
from app.commons import get_json_or_raise_exception
import app.exceptions as exceptions

account = Blueprint('account', __name__)


@account.route('/user')
@auth.login_required
def get_user_info():
    return jsonify(g.current_user.to_json()), 200


@account.route('/user', methods=['PUT'])
@auth.login_required
def update_user():
    for key, value in get_json_or_raise_exception().items():
        if key not in User.get_fields() or key == 'is_admin':
            raise exceptions.InvalidFieldException(payload={'field': key})
        if key == 'email' and g.current_user.email != value and User.objects.filter(email=value).first():
            raise exceptions.EmailTakenException
        elif key == 'username' and g.current_user.username != value and User.objects.filter(username=value).first():
            raise exceptions.UserExistsException
        setattr(g.current_user, key, value)
    g.current_user.save()
    return jsonify({'message': 'USER_UPDATED'}), 200


@account.route('/user', methods=['DELETE'])
@auth.login_required
def delete_account_request():
    user = g.current_user
    token = user.generate_token()
    send_email(user.email, 'Confirm account deletion', 'mail/delete_account', user=user, token=token)
    return jsonify({'sent_email': True}), 200


@account.route('/delete_account/<token>')
def delete_account(token):
    user = User.load_user_from_token(token)
    if user is not None:
        user.delete()
        flash('Your account has been deleted')
    else:
        flash('Failed to delete your account: token expired or was incorrect')
    return redirect(url_for('main.index'))
