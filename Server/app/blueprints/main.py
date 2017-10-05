from flask import jsonify, make_response, request, Blueprint, render_template, g

from app.celery.tasks import analyze_file_task, analyze_text_task
from app.http_auth import auth

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


@main.route('/text_files', methods=['POST'])
@auth.login_required
def post_text_file():
    print(request.get_data().decode("utf-8"))
    analyze_text_task.delay(request.get_data(), g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/')
def index():
    return render_template('base.html')
