from flask import Flask, jsonify, make_response, request, abort
from werkzeug.security import generate_password_hash, check_password_hash
from flask_httpauth import HTTPBasicAuth
import uuid
import os
import datetime
import mongodb
from celery_factory import make_celery
from vokaturi.analyzer import extract_emotions
from converter.amr2wav import convert


app = Flask(__name__)
app.config['CELERY_BROKER_URL'] = os.getenv('CELERY_BROKER_URL', 'amqp://guest:guest@localhost:5672//')
auth = HTTPBasicAuth()

celery = make_celery(app)
db = mongodb.get_db()
results_collection = db['results']
users_collection = db['users']


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes):
    filename = uuid.uuid4().hex
    amr_filename, wav_filename = f'{filename}.amr', f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(file_bytes)
    convert(amr_filename)
    emotions = extract_emotions(wav_filename)
    os.remove(amr_filename)
    os.remove(wav_filename)
    if emotions:
        db = mongodb.get_db()
        db['results'].insert({
            'user': 1,
            'datetime': datetime.datetime.now(),
            **emotions
        })


@app.route('/results', methods=['GET'])
def get_results_all():
    results = results_collection.find({}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/results/<int:user_id>', methods=['GET'])
def get_results_by_user(user_id):
    results = results_collection.find({'user': user_id}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/sound_files', methods=['POST'])
def post_sound_file():
    analyze_file_task.delay(request.get_data())
    return make_response(jsonify({'received': True}), 200)


@app.route('/register', methods=['POST'])
def register_user():
    request_json = request.get_json()
    username = request_json.get('username')
    password = request_json.get('password')
    if username is None or password is None:
        abort(400)
    if users_collection.find_one({'username': username}) is not None:
        abort(400)  # user already exists

    hashed_password = generate_password_hash(password)
    users_collection.insert({
        'username': username,
        'password_hash': hashed_password
    })
    return make_response(jsonify({'registered': True}), 201)


@auth.verify_password
def verify_password(username, password):
    user = users_collection.find_one({'username': username})
    if user is None:
        return False
    hashed_password = user['password_hash']
    return check_password_hash(hashed_password, password)


if __name__ == '__main__':
    app.run(debug=True)
