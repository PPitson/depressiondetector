from flask import jsonify, make_response, request, Blueprint, render_template

from app.http_auth import auth
from app.models import EmotionExtractionResult
from app.celery.tasks import analyze_file_task


main = Blueprint('main', __name__)


@main.route('/results', methods=['GET'])  # TODO: delete, this endpoint is only for testing
def get_results_all():
    results = EmotionExtractionResult.objects.exclude('id').all()
    return make_response(jsonify(list(results)), 200)


@main.route('/results/<username>', methods=['GET'])
@auth.login_required
def get_results_by_user(username):
    results = EmotionExtractionResult.objects.filter(username=username).exclude('id').all()
    return make_response(jsonify(list(results)), 200)


@main.route('/sound_files/<username>', methods=['POST'])
@auth.login_required
def post_sound_file(username):
    analyze_file_task.delay(request.get_data(), username)
    return make_response(jsonify({'received': True}), 200)


@main.route('/')
def index():
    return render_template('base.html')