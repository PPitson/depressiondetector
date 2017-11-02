from app.models import EmotionFromTextExtractionResult, HappinessLevel
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

    def test_happiness_level_is_saved_after_emotion_extraction_result_is_saved(self):
        emotions = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}
        EmotionFromTextExtractionResult.objects.create(user=self.user, **emotions)
        self.assertEqual(HappinessLevel.objects.count(), 1)

    def test_compute_happiness_level(self):
        emotions = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}
        emotion_result = EmotionFromTextExtractionResult(user=self.user, **emotions)
        self.assertEqual(emotion_result.compute_happiness_level(), 0.5)

    def test_is_subclass_of_data_source_mongo_document(self):
        emotions = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}
        emotion_result = EmotionFromTextExtractionResult(user=self.user, **emotions)
        self.assertTrue(isinstance(emotion_result, EmotionFromTextExtractionResult))