import indicoio
from celery import Celery
from flask import Flask
from flask_bootstrap import Bootstrap
from flask_googlemaps import GoogleMaps
from flask_mail import Mail
from flask_mongoengine import MongoEngine

from app.celery.factory import init_celery
from config import config, DEVELOPMENT_CONFIG_NAME

celery = Celery('app')
mail = Mail()
db = MongoEngine()
bootstrap = Bootstrap()


def create_app(config_name=DEVELOPMENT_CONFIG_NAME):
    app = Flask(__name__)
    app.config.from_object(config[config_name])

    register_extensions(app)
    register_blueprints(app)
    init_celery(app, celery)

    GoogleMaps(app)

    set_indico_key(config[config_name])

    return app


def set_indico_key(cfg):
    indicoio.config.api_key = cfg.INDICO_KEY


def register_extensions(app):
    mail.init_app(app)
    db.init_app(app)
    bootstrap.init_app(app)


def register_blueprints(app):
    from app.blueprints.main import main
    from app.blueprints.auth import auth
    from app.blueprints.account import account
    from app.blueprints.errors import errors
    app.register_blueprint(main)
    app.register_blueprint(auth)
    app.register_blueprint(account)
    app.register_blueprint(errors)
