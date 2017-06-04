from flask import Flask, jsonify, abort, make_response, request
import uuid
import os
from vokaturi.analyzer import analyze_file

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
    abort(404)


@app.route('/sound_files', methods=['POST'])
def post_sound_file():
    filename = uuid.uuid4().hex + '.wav'
    with open(filename, 'wb') as file:
        file.write(request.get_data())
    emotions = analyze_file(filename)
    print(emotions)
    # todo: save results to database
    os.remove(filename)
    if emotions:
        return make_response(jsonify({'received': True}), 200)
    return make_response(jsonify({'error': 'Failure while analyzing file'}), 400)


if __name__ == '__main__':
    app.run()
