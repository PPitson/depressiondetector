import json

from unittest.mock import patch
from datetime import datetime, timedelta

from tests.testcase import CustomTestCase
from app.models import HappinessLevel, Mood


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
    def test_post_sound_file(self, task_mock):
        data = b'123'
        response = self.client.post('/sound_files', headers=self.get_headers(), data=data)
        self.assert200(response)
        task_mock.delay.assert_called_with(data, self.user)
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

    def test_returns_json_missing_error(self):
        response = self.client.post('/moods', headers=self.get_headers(), data=json.dumps([]))
        self.check_response(response, 400, 'JSON_MISSING')

    def test_saves_mood_objects(self):
        data = json.dumps([
            {'date': '01-11-2017', 'mood': 2},
            {'date': '02-11-2017', 'mood': 3},
            {'date': '03-11-2017', 'mood': 3}
        ])
        self.assertEqual(Mood.objects.count(), 0)
        response = self.client.post('/moods', headers=self.get_headers(), data=data)
        self.assertStatus(response, 201)
        self.assertTrue(response.json['created'])
        self.assertEqual(Mood.objects.count(), 3)
        mood = Mood.objects.first()
        self.assertEqual(mood.user, self.user)
        self.assertEqual(mood.datetime, datetime(2017, 11, 1))
        self.assertEqual(mood.mood_level, 2)

    def test_invalid_mood_level(self):
        data = json.dumps([
            {'date': '03-11-2017', 'mood': 6}
        ])
        response = self.client.post('/moods', headers=self.get_headers(), data=data)
        self.assert400(response)
        self.assertIn('field', response.json)
        self.assertIn('message', response.json)
        self.assertEqual(response.json['field'], 'mood_level')
