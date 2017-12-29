import json
from datetime import datetime

import dateutil.parser
from flask import jsonify, make_response, request, Blueprint, render_template, g
from flask_googlemaps import Map
from mongoengine import PointField

from app.celery.tasks import analyze_file_task, analyze_text_task, save_result
from app.commons import get_json_list_or_raise_exception
from app.http_auth import auth, admins_only
from app.models import Mood
from map_util import get_dates_by_slider, prepare_sentiment_rects
from app.exceptions import JSONMissingException

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


@main.route('/mean_results', methods=['GET'])
@auth.login_required
def get_mean_results():
    results = g.current_user.mean_results
    return make_response(jsonify(results), 200)


@main.route('/sound_files', methods=['POST'])
@auth.login_required
def post_sound_files():
    if request.form['data'] == '':
        raise JSONMissingException
    files_metadata = json.loads(request.form['data'])
    for file in request.files.values():
        date_time = datetime.strptime(files_metadata[file.filename]['date'], '%Y-%m-%d %H:%M:%S')
        analyze_file_task.delay(file.read(), date_time, g.current_user, files_metadata[file.filename]['location'])
    return make_response(jsonify({'received': True}), 200)


@main.route('/text_files', methods=['POST'])
@auth.login_required
def post_text_files():
    request_json = get_json_list_or_raise_exception()
    for json in request_json:
        message = json['message']
        datetime = dateutil.parser.parse(json['datetime'])
        analyze_text_task.delay(message, datetime, g.current_user, json['location'])
    return make_response(jsonify({'received': True}), 200)


@main.route('/moods', methods=['POST'])
@auth.login_required
def post_moods():
    mood_results = get_json_list_or_raise_exception()
    for result in mood_results:
        mood = Mood(user=g.current_user, datetime=datetime.strptime(result['date'], '%Y-%m-%d'),
                    mood_level=result['mood_level'], coordinates=result['location'])
        mood.validate()
        save_result.delay(mood)
    return make_response(jsonify({'received': True}), 200)


@main.route('/map', methods=['GET', 'POST'])
@auth.login_required
@admins_only
def get_map():
    min, max = 1, 5
    slider = int(request.form['days']) if request.method == 'POST' else max
    date_start, date_end = get_dates_by_slider(slider, min, max)
    map = Map(zoom=2, identifier='mymap', lat=0, lng=0,
              style='height:400px;width:100%;margin-top:10px;margin-bottom:10px;', streetview_control=False,
              rectangles=prepare_sentiment_rects(date_start, date_end))
    return render_template('map.html', mymap=map, max=max, min=min, slider=slider)


@main.route('/')
def index():
    return render_template('base.html')
