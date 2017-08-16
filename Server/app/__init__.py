from config import Config
from celery import Celery
from flask import Flask
from flask_mail import Mail
from flask_mongoengine import MongoEngine

from app.celery.factory import init_celery


celery = Celery('app')
mail = Mail()
db = MongoEngine()


def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    mail.init_app(app)
    db.init_app(app)
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