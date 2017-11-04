from datetime import datetime

from flask import jsonify, make_response, request, Blueprint, render_template, g

from app.blueprints.auth import get_json_or_raise_exception
from app.celery.tasks import analyze_file_task, analyze_text_task
from app.http_auth import auth
from app.models import Mood

main = Blueprint('main', __name__)


@main.route('/voice_results', methods=['GET'])
@auth.login_required
def get_voice_results_by_user():
    results = g.current_user.voice_results
    return make_response(jsonify(list(results)), 200)


@main.route('/text_results', methods=['GET'])
@auth.login_required
def get_text_results_by_user():
    results = g.current_user.text_results
    return make_response(jsonify(list(results)), 200)


@main.route('/mood_results', methods=['GET'])
@auth.login_required
def get_mood_results_by_user():
    results = g.current_user.mood_results
    return make_response(jsonify(list(results)), 200)


@main.route('/sound_files', methods=['POST'])
@auth.login_required
def post_sound_file():
    analyze_file_task.delay(request.get_data(), g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/text_files', methods=['POST'])
@auth.login_required
def post_text_file():
    request_json = get_json_or_raise_exception()
    message = request_json['message']
    analyze_text_task.delay(message, g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/moods', methods=['POST'])
@auth.login_required
def post_moods():
    mood_results = get_json_or_raise_exception()
    for result in mood_results:
        Mood.objects.create(user=g.current_user, datetime=datetime.strptime(result['date'], '%d-%m-%Y'),
                            mood_level=result['mood'])
    return make_response(jsonify({'created': True}), 201)


@main.route('/')
def index():
    return render_template('base.html')
