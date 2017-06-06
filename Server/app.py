from flask import Flask, jsonify, abort, make_response, request
import uuid
import os
from pymongo import MongoClient
from vokaturi.analyzer import analyze_file
from converter.amr2wav import convert

app = Flask(__name__)


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


@app.route('/results', methods=['GET'])
def get_results_all():
    # todo: get all results
    abort(404)


@app.route('/results/<user_id>', methods=['GET'])
def get_results_by_user(user_id):
    # todo: get results of one user
    collection = MongoClient(os.getenv('MONGOLAB_URI'))['depressiondata']['results']
    result = collection.find({'user': str(user_id)})
    mess = ''
    for res in result:
        mess += str(res) + '\n'
    return make_response(jsonify(message=mess), 200)


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
        return make_response(jsonify({'received': True}), 200)
    return make_response(jsonify({'error': 'Failure while analyzing file'}), 400)



if __name__ == '__main__':
    app.run()
