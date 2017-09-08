import json
import time

from tests.testcase import CustomTestCase
from app.models import User
from app import mail


class AuthTestCase(CustomTestCase):

    endpoint = '/auth'

    def send_post_request(self, data):
        return self.client.post(self.endpoint, data=json.dumps(data), content_type='application/json')

    def send_empty_request(self):
        response = self.client.post(self.endpoint)
        self.check_response(response, 400, 'JSON_MISSING')


class RegisterTestCase(AuthTestCase):

    endpoint = AuthTestCase.endpoint + '/register'

    def test_successful_register(self):
        data = {'username': 'bob', 'email': 'bob@bob.com', 'password': 'alice'}
        response = self.send_post_request(data)
        self.check_response(response, 201, 'SIGNUP_USER_REGISTERED')
        self.assertIsNotNone(User.objects.filter(username='bob').first())

    def test_username_exists(self):
        data = {'username': self.user.username, 'email': 'bob@bob.com', 'password': 'alice'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'SIGNUP_LOGIN_ALREADY_USED')

    def test_email_exists(self):
        data = {'username': 'mary', 'email': self.user.email, 'password': 'alice'}
        response = self.send_post_request(data)
        self.check_response(response, 400,  'SIGNUP_EMAIL_ALREADY_USED')

    def test_username_missing(self):
        data = {'email': 'bob@bob.com', 'password': 'alice'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'FIELD_REQUIRED')
        self.assertEqual(response.json['field'], 'username')

    def test_email_missing(self):
        data = {'username': 'bob', 'password': 'alice'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'FIELD_REQUIRED')
        self.assertEqual(response.json['field'], 'email')

    def test_password_missing(self):
        data = {'username': 'bob', 'email': 'bob@bob.com'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'FIELD_REQUIRED')
        self.assertEqual(response.json['field'], 'password')

    def test_invalid_sex(self):
        data = {'username': 'bob', 'email': 'bob@bob.com', 'password': 'alice', 'sex': 'P'}
        response = self.send_post_request(data)
        self.check_response(response, 400, "Value must be one of ('M', 'F')")
        self.assertEqual(response.json['field'], 'sex')

    def test_invalid_date_of_birth_format(self):
        data = {'username': 'bob', 'email': 'bob@bob.com', 'password': 'alice', 'date_of_birth': '123456'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'cannot parse date "123456"')
        self.assertEqual(response.json['field'], 'date_of_birth')

    def test_data_missing(self):
        self.send_empty_request()


class LoginTestCase(AuthTestCase):

    endpoint = AuthTestCase.endpoint + '/login'

    def test_successful_login(self):
        data = {'username': self.user.username, 'password': self.password}
        response = self.send_post_request(data)
        self.check_response(response, 200, 'LOGIN_USER_LOGGED_IN')

    def test_incorrect_password(self):
        data = {'username': self.user.username, 'password': 'incorrect_password'}
        response = self.send_post_request(data)
        self.check_response(response, 401, 'LOGIN_PASSWORD_INVALID')

    def test_invalid_username(self):
        data = {'username': 'nonexisting_bob', 'password': self.password}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'LOGIN_LOGIN_DOES_NOT_EXIST')

    def test_data_missing(self):
        self.send_empty_request()


class ResetPasswordTestCase(AuthTestCase):

    endpoint = AuthTestCase.endpoint + '/reset_password'

    def check_reset_password_response(self, token, expected_flashed_message):
        response = self.client.post(f'{AuthTestCase.endpoint}/reset_password/{token}',
                                    data={'password': 'alice', 'password2': 'alice'})
        self.assertRedirects(response, '/')
        self.assertMessageFlashed(expected_flashed_message)

    def test_successful_reset_password_request(self):
        data = {'email': self.user.email}
        with mail.record_messages() as outbox:
            response = self.send_post_request(data)
            self.assertEqual(len(outbox), 1)
            message = outbox[0]
            self.assertEqual(message.subject, 'Reset your password')
            self.assertEqual(message.recipients, [self.user.email])
            self.assertTemplateUsed('mail/reset_password.html')
        self.assert200(response)
        self.assertTrue(response.json['sent_email'])

    def test_invalid_email(self):
        data = {'email': 'nonexisting@email.com'}
        response = self.send_post_request(data)
        self.check_response(response, 400, 'LOGIN_EMAIL_DOES_NOT_EXIST')

    def test_reset_password_page_get_request(self):
        response = self.client.get(AuthTestCase.endpoint + '/reset_password/some_token')
        self.assert200(response)
        self.assertTemplateUsed('reset_password.html')

    def test_sucessful_reset_password_form(self):
        token = self.user.generate_token().decode()
        password_hash_before = self.user.password_hash
        self.check_reset_password_response(token, 'Your password has been updated.')
        updated_user = User.objects.get(username=self.user.username)
        self.assertNotEqual(updated_user.password_hash, password_hash_before)

    def test_token_expired(self):
        token = self.user.generate_token(expiration=1).decode()
        password_hash_before = self.user.password_hash
        time.sleep(2)
        self.check_reset_password_response(
            token, 'Failed to update your password: token expired or was incorrect, or account was deleted')
        user = User.objects.get(username=self.user.username)
        self.assertEqual(user.password_hash, password_hash_before)

    def test_incorrect_token(self):
        token = '123'
        self.check_reset_password_response(
            token, 'Failed to update your password: token expired or was incorrect, or account was deleted')

    def test_invalid_token_after_account_deletion(self):
        token = self.user.generate_token().decode()
        self.user.delete()
        self.check_reset_password_response(
            token, 'Failed to update your password: token expired or was incorrect, or account was deleted')

    def test_data_missing(self):
        self.send_empty_request()