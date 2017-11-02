from datetime import datetime, timedelta

from tests.testcase import CustomTestCase
from app.models import HappinessLevel, EmotionExtractionResult, EmotionFromTextExtractionResult


class DataSourceMongoDocumentTestCase(CustomTestCase):

    def test_computes_average_of_voice_emotion_extraction_results_after_save(self):
        now = datetime.utcnow()
        yesterday = now - timedelta(days=1)
        self.assertEqual(HappinessLevel.objects.count(), 0)
        EmotionExtractionResult.objects.create(user=self.user, datetime=yesterday, neutral=0.1, happy=0.3, sad=0.1,
                                               angry=0.1, fear=0.4)
        self.assertEqual(HappinessLevel.objects.count(), 1)
        self.assertEqual(HappinessLevel.objects.first().voice_happiness_level, 0.3)
        EmotionExtractionResult.objects.create(user=self.user, neutral=0.3, happy=0.55, sad=0.5, angry=0.07, fear=0.03)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        self.assertEqual(HappinessLevel.objects[1].voice_happiness_level, 0.55)
        EmotionExtractionResult.objects.create(user=self.user, neutral=0.6, happy=0.25, sad=0.5, angry=0.07, fear=0.03)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        self.assertEqual(HappinessLevel.objects[1].voice_happiness_level, 0.4)

    def test_computes_average_of_text_emotion_extraction_results_after_save(self):
        now = datetime.utcnow()
        yesterday = now - timedelta(days=1)
        self.assertEqual(HappinessLevel.objects.count(), 0)
        EmotionFromTextExtractionResult.objects.create(user=self.user, datetime=yesterday, surprise=0.1, joy=0.3,
                                                       sadness=0.1, anger=0.1, fear=0.4)
        self.assertEqual(HappinessLevel.objects.count(), 1)
        self.assertEqual(HappinessLevel.objects.first().text_happiness_level, 0.3)
        EmotionFromTextExtractionResult.objects.create(user=self.user, surprise=0.3, joy=0.55, sadness=0.5, anger=0.07,
                                                       fear=0.03)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        self.assertEqual(HappinessLevel.objects[1].text_happiness_level, 0.55)
        EmotionFromTextExtractionResult.objects.create(user=self.user, surprise=0.6, joy=0.25, sadness=0.5, anger=0.07,
                                                       fear=0.03)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        self.assertEqual(HappinessLevel.objects[1].text_happiness_level, 0.4)