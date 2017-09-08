from tests.testcase import CustomTestCase
from app.models import EmotionExtractionResult


class EmotionExtractionResultModelTestCase(CustomTestCase):

    def test_save_emotion_extraction_result(self):
        emotions = {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1}
        EmotionExtractionResult.objects.create(user=self.user, **emotions)
        emotion_result = EmotionExtractionResult.objects.first()
        self.assertIsNotNone(emotion_result)
        self.assertEqual(emotion_result.user, self.user)

    def test_emotion_results_are_deleted_after_user_is_deleted(self):
        emotions_dicts = [
            {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1},
            {'happy': 0.4, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.2},
            {'happy': 0.3, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.3}
        ]
        for emotions in emotions_dicts:
            EmotionExtractionResult.objects.create(user=self.user, **emotions)
        self.assertEqual(EmotionExtractionResult.objects.count(), 3)
        self.user.delete()
        self.assertEqual(EmotionExtractionResult.objects.count(), 0)
