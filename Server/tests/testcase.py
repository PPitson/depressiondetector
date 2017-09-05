from flask_testing import TestCase
from base64 import b64encode

from app import create_app as create_application, db
from app.models import User
from config import TESTING_CONFIG_NAME


class CustomTestCase(TestCase):

    def create_app(self):
        app = create_application(config_name=TESTING_CONFIG_NAME)
        self.db_name = app.config['TEST_DB_NAME']
        return app

    def setUp(self):
        self.user = User(username='admin', email='a@b.com')
        self.user.password = 'pass'
        self.user.save()
        self.password = 'pass'

    def tearDown(self):
        db.connection.drop_database(self.db_name)  # drop all collections

    def get_headers(self, username='admin', password='pass'):
        return {
            'Authorization': 'Basic ' + b64encode(f'{username}:{password}'.encode()).decode(),
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }