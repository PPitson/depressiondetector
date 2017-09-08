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

