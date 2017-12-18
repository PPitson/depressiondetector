import json
from datetime import datetime, timedelta
from io import BytesIO
from unittest.mock import patch, call

from app.models import HappinessLevel, Mood
from tests.testcase import CustomTestCase


class VoiceEmotionResultsTestCase(CustomTestCase):
    def test_unauthorized_get_voice_results(self):
        response = self.client.get('/voice_results')
        self.assert401(response)

    def test_get_voice_results(self):
        now = datetime.utcnow().date()
        for i in range(3):
            HappinessLevel.objects.create(user=self.user, date=now - timedelta(days=i),
                                          voice_happiness_level=0.1 * i, text_happiness_level=0.1 * i)

        response = self.client.get('/voice_results', headers=self.get_headers())
        self.assert200(response)
        self.assertEqual(len(response.json), 3)
        for result in response.json:
            self.assertIn('voice_happiness_level', result)
            self.assertIn('date', result)
            self.assertNotIn('text_happiness_level', result)

    def test_unauthorized_post_sound_file(self):
        response = self.client.post('/sound_files', data=b'123')
        self.assert401(response)

    @patch('app.blueprints.main.analyze_file_task')
    def test_post_sound_files(self, task_mock):
        date1 = '2017-11-11 16:24:35'
        date2 = '2017-11-11 18:27:38'
        data = {
            'data': json.dumps({
                'file.amr': {'date': date1},
                'another.amr': {'date': date2},
            }),
            'file1': (BytesIO(b'file bytes'), 'file.amr'),
            'file2': (BytesIO(b'another file bytes'), 'another.amr')
        }
        response = self.client.post('/sound_files', headers=self.create_auth_header(),
                                    content_type='multipart/form-data', data=data)
        self.assert200(response)
        calls = [
            call(b'file bytes', datetime(2017, 11, 11, 16, 24, 35), self.user),
            call(b'another file bytes', datetime(2017, 11, 11, 18, 27, 38), self.user)
        ]
        task_mock.delay.assert_has_calls(calls, any_order=True)
        self.assertTrue(response.json['received'])


class TextEmotionResultsTestCase(CustomTestCase):
    def test_unauthorized_get_text_results(self):
        response = self.client.get('/text_results')
        self.assert401(response)

    def test_get_text_results(self):
        now = datetime.utcnow().date()
        for i in range(3):
            HappinessLevel.objects.create(user=self.user, date=now - timedelta(days=i),
                                          voice_happiness_level=0.1 * i, text_happiness_level=0.1 * i)

        response = self.client.get('/text_results', headers=self.get_headers())
        self.assert200(response)
        self.assertEqual(len(response.json), 3)
        for result in response.json:
            self.assertIn('text_happiness_level', result)
            self.assertIn('date', result)
            self.assertNotIn('voice_happiness_level', result)


class MoodResultsTestCase(CustomTestCase):
    def test_unauthorized_get_mood_results(self):
        response = self.client.get('/mood_results')
        self.assert401(response)

    def test_get_mood_results(self):
        now = datetime.utcnow().date()
        for i in range(4):
            HappinessLevel.objects.create(user=self.user, date=now - timedelta(days=i),
                                          voice_happiness_level=0.1 * i, text_happiness_level=0.1 * i,
                                          mood_happiness_level=0.3 * i)

        response = self.client.get('/mood_results', headers=self.get_headers())
        self.assert200(response)
        self.assertEqual(len(response.json), 4)
        for result in response.json:
            self.assertIn('mood_happiness_level', result)
            self.assertIn('date', result)
            self.assertNotIn('voice_happiness_level', result)
            self.assertNotIn('text_happiness_level', result)

    def test_unauthorized_post_moods(self):
        response = self.client.post('/moods', data=json.dumps({'a': 1}), content_type='application/json')
        self.assert401(response)

    def test_returns_json_list_missing_error(self):
        response = self.client.post('/moods', headers=self.get_headers(), data=b'123')
        self.check_response(response, 400, 'JSON_LIST_MISSING')
        response = self.client.post('/moods', headers=self.get_headers(), data=json.dumps({}))
        self.check_response(response, 400, 'JSON_LIST_MISSING')
        response = self.client.post('/moods', headers=self.get_headers(), data=json.dumps([]))
        self.check_response(response, 400, 'JSON_LIST_MISSING')
        response = self.client.post('/moods', headers=self.get_headers(), data=json.dumps({'a': 1}))
        self.check_response(response, 400, 'JSON_LIST_MISSING')

    @patch('app.blueprints.main.save_result')
    def test_post_moods(self, save_result_mock):
        data = json.dumps([
            {'date': '2017-11-01', 'mood_level': 2},
            {'date': '2017-11-02', 'mood_level': 3},
            {'date': '2017-11-03', 'mood_level': 3}
        ])
        self.assertEqual(Mood.objects.count(), 0)
        response = self.client.post('/moods', headers=self.get_headers(), data=data)
        self.assertStatus(response, 200)
        self.assertTrue(response.json['received'])

        self.assertEqual(save_result_mock.delay.call_count, 3)

    def test_invalid_mood_level(self):
        data = json.dumps([
            {'date': '2017-11-03', 'mood_level': 6}
        ])
        response = self.client.post('/moods', headers=self.get_headers(), data=data)
        self.assert400(response)
        self.assertIn('field', response.json)
        self.assertIn('message', response.json)
        self.assertEqual(response.json['field'], 'mood_level')
