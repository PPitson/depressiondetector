from flask import abort
from flask_httpauth import HTTPBasicAuth
from werkzeug.security import check_password_hash
import mongodb
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
    return check_password_hash(hashed_password, password)


def verify_username(f):

    @wraps(f)
    def inner(username, *args, **kwargs):
        if auth.username() != username:
            abort(401)
        return f(username, *args, **kwargs)

    return inner
