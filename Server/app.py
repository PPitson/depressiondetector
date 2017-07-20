from flask import Flask, jsonify, make_response, request
import uuid
import os
import datetime
from pymongo import MongoClient
from celery_factory import make_celery
from vokaturi.analyzer import extract_emotions
from converter.amr2wav import convert


app = Flask(__name__)
app.config['CELERY_BROKER_URL'] = os.getenv('CELERY_BROKER_URL', 'amqp://guest:guest@localhost:5672//')
celery = make_celery(app)

client = MongoClient(os.getenv('MONGOLAB_URI'))
collection = client['depressiondata']['results']


@celery.task(name='analyze_file')
def analyze_file_task(amr_filename, wav_filename):
    convert(amr_filename)
    emotions = extract_emotions(wav_filename)
    os.remove(amr_filename)
    os.remove(wav_filename)
    if emotions:
        collection.insert({
            'user': 1,
            'datetime': datetime.datetime.now(),
            **emotions
        })


@app.route('/results', methods=['GET'])
def get_results_all():
    results = collection.find({}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/results/<int:user_id>', methods=['GET'])
def get_results_by_user(user_id):
    results = collection.find({'user': user_id}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@app.route('/sound_files', methods=['POST'])
def post_sound_file():
    filename = uuid.uuid4().hex
    amr_filename, wav_filename = f'{filename}.amr', f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(request.get_data())
    analyze_file_task.delay(amr_filename, wav_filename)
    return make_response(jsonify({'received': True}), 200)


if __name__ == '__main__':
    app.run(debug=True)
