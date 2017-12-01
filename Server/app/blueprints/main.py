import json
from datetime import datetime, timedelta

import dateutil.parser
import geohash
from flask import jsonify, make_response, request, Blueprint, render_template, g
# from flask_googlemaps import Map
from flask_googlemaps import Map

from app.celery.tasks import analyze_file_task, analyze_text_task
from app.commons import get_json_list_or_raise_exception
from app.http_auth import auth
from app.models import Mood, GeoSentiment
from map_util import sent2r, sent2g, rgb2hex

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
    files_metadata = json.loads(request.form['data'])
    for file in request.files.values():
        date_time = datetime.strptime(files_metadata[file.filename]['date'], '%Y-%m-%d %H:%M:%S')
        analyze_file_task.delay(file.read(), date_time, g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/text_files', methods=['POST'])
@auth.login_required
def post_text_files():
    request_json = get_json_list_or_raise_exception()
    for json in request_json:
        message = json['message']
        datetime = dateutil.parser.parse(json['datetime'])
        analyze_text_task.delay(message, datetime, g.current_user)
    return make_response(jsonify({'received': True}), 200)


@main.route('/moods', methods=['POST'])
@auth.login_required
def post_moods():
    mood_results = get_json_list_or_raise_exception()
    for result in mood_results:
        Mood.objects.create(user=g.current_user, datetime=datetime.strptime(result['date'], '%Y-%m-%d'),
                            mood_level=result['mood'])
    return make_response(jsonify({'created': True}), 201)


@main.route('/map', methods=['GET'])
@auth.login_required
def get_map():
    yesterday_date = datetime.now().date() - timedelta(days=1)
    rects = []
    for geo_sentiment in GeoSentiment.objects(date__gte=yesterday_date):
        bounds = geohash.bbox(geo_sentiment.geohash)
        sentiment = geo_sentiment.mean_sentiment
        rects.append({
            'fill_color': rgb2hex(sent2r(s=sentiment), sent2g(s=sentiment), 0),
            'stroke_weight': 0,
            'fill_opacity': 0.5,
            'bounds': {
                'north': bounds['n'],
                'west': bounds['w'],
                'south': bounds['s'],
                'east': bounds['e']
            }
        })

    map = Map(zoom=2, identifier='mymap', lat=0, lng=0,
              style='height:400px;width:100%;margin-top:10px;margin-bottom:10px;', streetview_control=False,
              rectangles=rects)
    return render_template('map.html', mymap=map)


@main.route('/')
def index():
    return render_template('base.html')
