from flask import abort, g
from flask_httpauth import HTTPBasicAuth
from werkzeug.security import check_password_hash
from app import mongodb
from app.models import User
from functools import wraps

auth = HTTPBasicAuth()


@auth.verify_password
def verify_password(username, password):
    db = mongodb.get_db()
    users_collection = db['users']
    user = users_collection.find_one({'username': username})
    if user is None:
        return False
    hashed_password = user['password_hash']
    g.current_user = User(**user)
    return check_password_hash(hashed_password, password)


def verify_username(f):

    @wraps(f)
    def inner(username, *args, **kwargs):
        if auth.username() != username:
            abort(403)
        return f(username, *args, **kwargs)

    return inner
