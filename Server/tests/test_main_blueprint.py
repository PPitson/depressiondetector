from unittest.mock import patch
from datetime import datetime, timedelta

from tests.testcase import CustomTestCase
from app.models import HappinessLevel


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
