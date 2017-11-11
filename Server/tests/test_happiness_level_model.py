from datetime import datetime

from app.models import HappinessLevel
from tests.testcase import CustomTestCase


class HappinessLevelModelTestCase(CustomTestCase):
    def test_happiness_level_save(self):
        self.assertEqual(HappinessLevel.objects.count(), 0)
        happiness_level = HappinessLevel(user=self.user, voice_happiness_level=0.2)
        happiness_level.save()
        self.assertEqual(HappinessLevel.objects.count(), 1)
        self.assertEqual(HappinessLevel.objects.first(), happiness_level)
        today = datetime.utcnow().date()
        self.assertEqual(happiness_level.date, today)

    def test_get_happiness_level_results_for_nonexisting_user(self):
        HappinessLevel.objects.create(user=self.user, text_happiness_level=0.4)
        self.assertEqual(len(HappinessLevel.get_results(self.user, 'text')), 1)
        self.user.delete()
        self.assertEqual(len(HappinessLevel.get_results(self.user, 'text')), 0)

    def test_get_happiness_level_results_by_invalid_data_source(self):
        with self.assertRaises(AssertionError):
            HappinessLevel.get_results(self.user, 'non_existing_data_source')
