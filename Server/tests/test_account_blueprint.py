import json
import time
from base64 import b64encode
from datetime import datetime

from app.models import User
from tests.testcase import CustomTestCase
from app import mail


class GetUserInfoTestCase(CustomTestCase):
    endpoint = '/user'

    def test_unauthorized_get_user_info_request(self):
        response = self.client.get(self.endpoint)
        self.assert401(response)

    def test_successful_get_user_info_request(self):
        self.user.date_of_birth = datetime(1995, 1, 23, 12, 34, 45)
        self.user.sex = 'M'
        self.user.save()
        response = self.client.get(self.endpoint, headers=self.get_headers())
        self.assert200(response)
        self.assertSetEqual(set(response.json.keys()), {'username', 'email', 'sex', 'date_of_birth'})
        self.assertEqual(response.json['username'], self.user.username)
        self.assertEqual(response.json['email'], self.user.email)
        self.assertEqual(response.json['sex'], self.user.sex)
        self.assertEqual(response.json['date_of_birth'], '1995-01-23')


class UpdateAccountTestCase(CustomTestCase):
    endpoint = '/user'

    def send_put_request(self, data):
        return self.client.put(self.endpoint, headers=self.get_headers(), data=json.dumps(data))

    def test_unauthorized_update_user_request(self):
        response = self.client.put(self.endpoint)
        self.assert401(response)

    def test_data_missing_with_put_request(self):
        response = self.client.put(
            self.endpoint,
            headers={'Authorization': 'Basic ' + b64encode(f'{self.user.username}:{self.password}'.encode()).decode()}
        )
        self.check_response(response, 400, 'JSON_MISSING')

    def test_successful_update_user(self):
        data = {'username': 'bob', 'email': 'b@b.com', 'sex': 'F', 'password': 'bobby', 'date_of_birth': '1995-10-20',
                'contact_person_email': 'emma@emma.com', 'contact_person_phone': '123456789'}
        response = self.send_put_request(data)
        self.check_response(response, 200, 'USER_UPDATED')
        updated_user = User.objects.filter(pk=self.user.pk).first()
        self.assertEqual(updated_user.username, 'bob')
        self.assertEqual(updated_user.sex, 'F')
        self.assertEqual(updated_user.email, 'b@b.com')
        self.assertEqual(updated_user.date_of_birth.strftime('%Y-%m-%d'), '1995-10-20')
        self.assertEqual(updated_user.contact_person_email, 'emma@emma.com')
        self.assertEqual(updated_user.contact_person_phone, '123456789')
        self.assertNotEqual(updated_user.password_hash, self.user.password_hash)

    def test_email_taken(self):
        another_user = User.objects.create(username='amelia', email='la@la.com', password_hash='secr')
        data = {'email': another_user.email}
        response = self.send_put_request(data)
        self.check_response(response, 400, 'SIGNUP_EMAIL_ALREADY_USED')

    def test_same_email_doesnt_produce_error(self):
        data = {'email': self.user.email}
        response = self.send_put_request(data)
        self.check_response(response, 200, 'USER_UPDATED')

    def test_username_taken(self):
        another_user = User.objects.create(username='amelia', email='la@la.com', password_hash='secr')
        data = {'username': another_user.username}
        response = self.send_put_request(data)
        self.check_response(response, 400, 'SIGNUP_LOGIN_ALREADY_USED')

    def test_same_username_doesnt_produce_error(self):
        data = {'username': self.user.username}
        response = self.send_put_request(data)
        self.assert200(response)

    def test_invalid_sex(self):
        data = {'sex': 'P'}
        response = self.send_put_request(data)
        self.check_response(response, 400, "Value must be one of ('M', 'F')")
        self.assertEqual(response.json['field'], 'sex')

    def test_invalid_date_of_birth_format(self):
        data = {'date_of_birth': 'what_is_this'}
        response = self.send_put_request(data)
        self.check_response(response, 400, 'cannot parse date "what_is_this"')
        self.assertEqual(response.json['field'], 'date_of_birth')

    def test_invalid_field(self):
        data = {'nonexisting_field': 5}
        response = self.send_put_request(data)
        self.check_response(response, 400, 'INVALID_FIELD')
        self.assertEqual(response.json['field'], 'nonexisting_field')


class DeleteAccountTestCase(CustomTestCase):
    endpoint = '/user'

    def check_delete_account_response(self, token, expected_flashed_message):
        response = self.client.get(f'/delete_account/{token}')
        self.assertMessageFlashed(expected_flashed_message)
        self.assertRedirects(response, '/')

    def test_unauthorized_delete_user_request(self):
        response = self.client.delete(self.endpoint)
        self.assert401(response)

    def test_succesful_delete_user_request(self):
        with mail.record_messages() as outbox:
            response = self.client.delete(self.endpoint, headers=self.get_headers())
            self.assertEqual(len(outbox), 1)
            message = outbox[0]
            self.assertEqual(message.subject, 'Confirm account deletion')
            self.assertEqual(message.recipients, [self.user.email])
            self.assertTemplateUsed('mail/delete_account.html')
        self.assert200(response)
        self.assertTrue(response.json['sent_email'])

    def test_successful_delete_account(self):
        token = self.user.generate_token().decode()
        primary_key = self.user.pk
        self.check_delete_account_response(token, 'Your account has been deleted')
        self.assertIsNone(User.objects.filter(pk=primary_key).first())

    def test_token_expired(self):
        token = self.user.generate_token(expiration=1).decode()
        time.sleep(2)
        self.check_delete_account_response(token, 'Failed to delete your account: token expired or was incorrect')

    def test_invalid_token(self):
        token = 'bob'
        self.check_delete_account_response(token, 'Failed to delete your account: token expired or was incorrect')
