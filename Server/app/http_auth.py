from functools import wraps

from flask import g
from flask_httpauth import HTTPBasicAuth
from app.models import User
import app.exceptions as exceptions
import mongoengine as mongo

auth = HTTPBasicAuth()


@auth.verify_password
def verify_password(username, password):
    try:
        user = User.objects.get(username=username)
    except mongo.DoesNotExist:
        return False
    g.current_user = user
    return user.verify_password(password)


def admins_only(func):
    @wraps(func)
    def inner(*args, **kwargs):
        if not g.current_user.is_admin:
            raise exceptions.InsufficientPrivilegesException
        return func(*args, **kwargs)

    return inner
