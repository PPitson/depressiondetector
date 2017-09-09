from flask import jsonify, make_response, request, Blueprint, render_template, g

from app.http_auth import auth
from app.celery.tasks import analyze_file_task


main = Blueprint('main', __name__)


@main.route('/results', methods=['GET'])
@auth.login_required
def get_results_by_user():
    results = g.current_user.emotion_extraction_results
    return make_response(jsonify(list(results)), 200)


@main.route('/sound_files', methods=['POST'])
@auth.login_required
def post_sound_file():
    analyze_file_task.delay(request.get_data(), g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/')
def index():
    return render_template('base.html')
