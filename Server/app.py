from flask import Flask, jsonify, make_response, request, abort
from werkzeug.security import generate_password_hash
import uuid
import os
import datetime

import mongodb
from celery_factory import make_celery
from vokaturi.analyzer import extract_emotions
from converter.amr2wav import convert
from auth import auth, verify_username

app = Flask(__name__)
app.config['CELERY_BROKER_URL'] = os.getenv('CELERY_BROKER_URL', 'amqp://guest:guest@localhost:5672//')

celery = make_celery(app)
db = mongodb.get_db()
results_collection = db['results']
users_collection = db['users']


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, username):
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
            'user': username,
            'datetime': datetime.datetime.now(),
            **emotions
        })


@app.route('/results', methods=['GET'])  # TODO: delete, this endpoint is only for testing
def get_results_all():
    results = results_collection.find({}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/results/<username>', methods=['GET'])
@auth.login_required
@verify_username
def get_results_by_user(username):
    results = results_collection.find({'user': username}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/sound_files/<username>', methods=['POST'])
@auth.login_required
@verify_username
def post_sound_file(username):
    analyze_file_task.delay(request.get_data(), username)
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

    password_hash = generate_password_hash(password)
    users_collection.insert({
        'username': username,
        'password_hash': password_hash
    })
    return make_response(jsonify({'registered': True}), 201)


if __name__ == '__main__':
    app.run(debug=True)
