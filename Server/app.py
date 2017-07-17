from flask import Flask, jsonify, make_response, request
import uuid
import os
import datetime
from pymongo import MongoClient
from vokaturi.analyzer import analyze_file
from converter.amr2wav import convert


app = Flask(__name__)
client = MongoClient(os.getenv('MONGOLAB_URI'))
collection = client['depressiondata']['results']


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
    amr_filename = f'{filename}.amr'
    wav_filename = f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(request.get_data())
    convert(amr_filename)
    emotions = analyze_file(wav_filename)
    os.remove(amr_filename)
    os.remove(wav_filename)
    if not emotions:
        return make_response(jsonify({'error': 'Failure while analyzing file'}), 400)
    collection.insert({
        'user': 1,
        'datetime': datetime.datetime.now(),
        **emotions
    })
    return make_response(jsonify({'received': True}), 200)


if __name__ == '__main__':
    app.run()
