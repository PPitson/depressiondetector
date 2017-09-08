from tests.testcase import CustomTestCase
from app.http_auth import auth


class VoiceEmotionResultsTestCase(CustomTestCase):

    def create_app(self):
        app = super().create_app()

        @app.route('/test_http_auth')
        @auth.login_required
        def http_auth():
            return 'ok'

        return app

    def test_nonexisting_username(self):
        response = self.client.get('/test_http_auth', headers=self.get_headers('mike', 'cat'))
        self.assert401(response)

    def test_incorrect_password(self):
        response = self.client.get('/test_http_auth', headers=self.get_headers('admin', 'dog'))
        self.assert401(response)

    def test_correct_basic_http_auth(self):
        response = self.client.get('/test_http_auth', headers=self.get_headers('admin', 'pass'))
        self.assert200(response)