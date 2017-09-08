from unittest.mock import patch

from tests.testcase import CustomTestCase
from app.models import User, EmotionExtractionResult


class VoiceEmotionResultsTestCase(CustomTestCase):

    def setUp(self):
        super().setUp()
        result = EmotionExtractionResult(user=self.user, neutral=0.1, happy=0.3, sad=0.1, angry=0.1, fear=0.4)
        result.save()
        other_result = EmotionExtractionResult(user=self.user, neutral=0.3, happy=0.55, sad=0.5, angry=0.07, fear=0.03)
        other_result.save()
        self.result = result
        self.result2 = other_result

        self.bob = User(username='bob', email='bob@bob.com', password_hash='hash')
        self.bob.save()
        result = EmotionExtractionResult(user=self.bob, neutral=0.1, happy=0.3, sad=0.1, angry=0.1, fear=0.4)
        result.save()
        other_result = EmotionExtractionResult(user=self.bob, neutral=0.3, happy=0.55, sad=0.5, angry=0.07, fear=0.03)
        other_result.save()
        self.bob_result = result
        self.bob_result2 = other_result

    def test_unauthorized_get_results(self):
        response = self.client.get('/results')
        self.assert401(response)

    def test_get_results(self):
        response = self.client.get('/results', headers=self.get_headers())
        self.assert200(response)
        self.assertIn(self.result.to_json(), response.json)
        self.assertIn(self.result2.to_json(), response.json)
        self.assertEqual([self.result.to_json(), self.result2.to_json()], response.json)
        self.assertNotIn(self.bob_result.to_json(), response.json)
        self.assertNotIn(self.bob_result2.to_json(), response.json)

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