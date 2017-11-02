from tests.testcase import CustomTestCase
from app.models import EmotionExtractionResult, HappinessLevel, DataSourceMongoDocument


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

    def test_happiness_level_is_saved_after_emotion_extraction_result_is_saved(self):
        emotions = {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1}
        EmotionExtractionResult.objects.create(user=self.user, **emotions)
        self.assertEqual(HappinessLevel.objects.count(), 1)

    def test_compute_happiness_level(self):
        emotions = {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1}
        emotion_result = EmotionExtractionResult(user=self.user, **emotions)
        self.assertEqual(emotion_result.compute_happiness_level(), 0.5)

    def test_is_subclass_of_data_source_mongo_document(self):
        emotions = {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1}
        emotion_result = EmotionExtractionResult(user=self.user, **emotions)
        self.assertTrue(isinstance(emotion_result, DataSourceMongoDocument))