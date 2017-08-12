from flask import jsonify, make_response, request, Blueprint

from app.http_auth import auth, verify_username
from app import mongodb
from app.celery.tasks import analyze_file_task


main = Blueprint('main', __name__)

db = mongodb.get_db()
results_collection = db['results']


@main.route('/results', methods=['GET'])  # TODO: delete, this endpoint is only for testing
def get_results_all():
    results = results_collection.find({}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@main.route('/results/<username>', methods=['GET'])
@auth.login_required
@verify_username
def get_results_by_user(username):
    results = results_collection.find({'user': username}, {'_id': 0})
    return make_response(jsonify(list(results)), 200)


@main.route('/sound_files/<username>', methods=['POST'])
@auth.login_required
@verify_username
def post_sound_file(username):
    analyze_file_task.delay(request.get_data(), username)
    return make_response(jsonify({'received': True}), 200)
