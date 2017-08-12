import os
from celery import Celery
from flask import Flask

from app.celery.factory import init_celery


celery = Celery('app')


def create_app():
    app = Flask(__name__)
    app.config['CELERY_BROKER_URL'] = os.getenv('CELERY_BROKER_URL', 'amqp://guest:guest@localhost:5672//')

    register_blueprints(app)
    init_celery(app, celery)

    return app


def register_blueprints(app):
    from app.blueprints.main import main
    from app.blueprints.auth import auth
    from app.blueprints.errors import errors
    app.register_blueprint(main)
    app.register_blueprint(auth)
    app.register_blueprint(errors)