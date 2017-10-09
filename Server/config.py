import os


class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY', 'very secret key indeed')
    CELERY_BROKER_URL = os.environ.get('CELERY_BROKER_URL', 'amqp://guest:guest@localhost:5672//')
    MAIL_SERVER = 'smtp.googlemail.com'
    MAIL_PORT = 587
    MAIL_USE_TLS = True
    MAIL_USERNAME = os.environ.get('MAIL_USERNAME')
    MAIL_PASSWORD = os.environ.get('MAIL_PASSWORD')
    INDICO_KEY = os.environ.get('INDICO_KEY')


class DevelopmentConfig(Config):
    DEBUG = True
    MONGODB_HOST = os.environ.get('MONGOLAB_URI')


class TestingConfig(Config):
    TESTING = True
    MONGODB_HOST = os.environ.get('MONGOLAB_URI_TEST')
    TEST_DB_NAME = MONGODB_HOST.split('/')[-1]
    WTF_CSRF_ENABLED = False
    MAIL_USERNAME = 'tester'


DEVELOPMENT_CONFIG_NAME = 'development'
TESTING_CONFIG_NAME = 'testing'

config = {
    DEVELOPMENT_CONFIG_NAME: DevelopmentConfig,
    TESTING_CONFIG_NAME: TestingConfig
}
