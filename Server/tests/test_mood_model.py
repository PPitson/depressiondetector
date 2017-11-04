from tests.testcase import CustomTestCase
from app.models import HappinessLevel, Mood, DataSourceMongoDocument


class MoodModelTestCase(CustomTestCase):
    def test_save_mood(self):
        Mood.objects.create(user=self.user, mood_level=3)
        mood = Mood.objects.first()
        self.assertIsNotNone(mood)
        self.assertEqual(mood.user, self.user)

    def test_mood_results_are_deleted_after_user_is_deleted(self):
        mood_levels = (2, 5, 4)
        for mood_level in mood_levels:
            Mood.objects.create(user=self.user, mood_level=mood_level)
        self.assertEqual(Mood.objects.count(), 3)
        self.user.delete()
        self.assertEqual(Mood.objects.count(), 0)

    def test_happiness_level_is_saved_after_mood_is_saved(self):
        Mood.objects.create(user=self.user, mood_level=3)
        self.assertEqual(Mood.objects.count(), 1)
        self.assertEqual(HappinessLevel.objects.count(), 1)

    def test_compute_happiness_level(self):
        expected_happiness_levels = (0, 0.25, 0.5, 0.75, 1)
        for mood_level, expected_happiness_level in zip(range(1, 6), expected_happiness_levels):
            mood = Mood(user=self.user, mood_level=mood_level)
            self.assertEqual(mood.compute_happiness_level(), expected_happiness_level)

    def test_is_subclass_of_data_source_mongo_document(self):
        mood = Mood(user=self.user, mood_level=3)
        self.assertTrue(isinstance(mood, DataSourceMongoDocument))
