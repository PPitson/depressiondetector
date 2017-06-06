from flask import Flask, jsonify, make_response, request
import uuid
import os
import datetime
from pymongo import MongoClient
from vokaturi.analyzer import analyze_file
from converter.amr2wav import convert

app = Flask(__name__)


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found sth else'}), 404)


@app.route('/results', methods=['GET'])
def get_results_all():
    collection = MongoClient(os.getenv('MONGOLAB_URI'))['depressiondata']['results']
    result = collection.find()
    result_list = []
    for res in result:
        result_map = {}
        for single in res:
            result_map[single] = str(res[single])
        result_list.append(result_map)
    return make_response(jsonify(result_list), 200)


@app.route('/results/<user_id>', methods=['GET'])
def get_results_by_user(user_id):
    collection = MongoClient(os.getenv('MONGOLAB_URI'))['depressiondata']['results']
    result = collection.find({'user': int(user_id)})
    result_list = []
    for res in result:
        result_map = {}
        for single in res:
            result_map[single] = str(res[single])
        result_list.append(result_map)
    return make_response(jsonify(result_list), 200)


@app.route('/sound_files', methods=['POST'])
def post_sound_file():
    filename = uuid.uuid4().hex
    amr_filename = f'{filename}.amr'
    wav_filename = f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(request.get_data())
    convert(amr_filename)
    emotions = analyze_file(wav_filename)
    print(emotions)
    # todo: save results to database
    os.remove(amr_filename)
    os.remove(wav_filename)
    if emotions:
        collection = MongoClient(os.getenv('MONGOLAB_URI'))['depressiondata']['results']
        collection.insert({
            'user': 1,
            'datetime': datetime.datetime.now(),
            'neutral': round(emotions['neutral'], 3),
            'happy': round(emotions['happy'], 3),
            'sad': round(emotions['sad'], 3),
            'angry': round(emotions['angry'], 3),
            'fear': round(emotions['fear'], 3)
        })
        return make_response(jsonify({'received': True}), 200)
    return make_response(jsonify({'error': 'Failure while analyzing file'}), 400)


if __name__ == '__main__':
    app.run()
