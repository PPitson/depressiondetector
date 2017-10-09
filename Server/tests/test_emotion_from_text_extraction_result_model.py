from app.models import EmotionFromTextExtractionResult
from tests.testcase import CustomTestCase


class EmotionExtractionFromTextResultModelTestCase(CustomTestCase):
    def test_save_emotion_from_text_extraction_result(self):
        emotions = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}
        EmotionFromTextExtractionResult.objects.create(user=self.user, **emotions)
        emotion_result = EmotionFromTextExtractionResult.objects.first()
        self.assertIsNotNone(emotion_result)
        self.assertEqual(emotion_result.user, self.user)

    def test_emotion_from_text_results_are_deleted_after_user_is_deleted(self):
        emotions_dicts = [
            {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1},
            {'joy': 0.4, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.2},
            {'joy': 0.3, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.3}
        ]
        for emotions in emotions_dicts:
            EmotionFromTextExtractionResult.objects.create(user=self.user, **emotions)
        self.assertEqual(EmotionFromTextExtractionResult.objects.count(), 3)
        self.user.delete()
        self.assertEqual(EmotionFromTextExtractionResult.objects.count(), 0)
