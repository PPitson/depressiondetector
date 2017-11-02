import time
from datetime import datetime, timedelta

from tests.testcase import CustomTestCase
from app.models import User, EmotionExtractionResult, EmotionFromTextExtractionResult, HappinessLevel


class UserModelTest(CustomTestCase):

    def test_user_save(self):
        saved_user = User.objects.first()
        self.assertEqual(self.user, saved_user)
        other_user = User(username='john', email='john@john.com', password_hash='quick')
        other_user.save()
        self.assertEqual(self.user, User.objects.filter(username='admin').first())

    def test_password_hash_was_set(self):
        self.assertIsNotNone(self.user.password_hash)

    def test_cant_access_password(self):
        with self.assertRaises(AttributeError):
            _ = self.user.password

    def test_verify_password(self):
        self.assertTrue(self.user.verify_password('pass'))
        self.assertFalse(self.user.verify_password('wrong_pass'))

    def test_password_hashes_different_for_same_passwords(self):
        other_user = User(username='bob', email='bob@bob.com')
        other_user.password = 'pass'
        self.assertTrue(other_user.password_hash != self.user.password_hash)

    def test_load_user_from_token(self):
        token = self.user.generate_token()
        user = User.load_user_from_token(token)
        self.assertEqual(self.user, user)

    def test_load_user_from_token_fails_after_deleting_user(self):
        token = self.user.generate_token()
        self.user.delete()
        self.assertIsNone(User.load_user_from_token(token))

    def test_token_expiration_date(self):
        token = self.user.generate_token(expiration=1)
        time.sleep(2)
        user = User.load_user_from_token(token)
        self.assertIsNone(user)

    def test_user_voice_results(self):
        now = datetime.utcnow()
        yesterday = now - timedelta(days=1)
        date_format = '%d-%m-%Y'
        EmotionExtractionResult.objects.create(user=self.user, datetime=yesterday, neutral=0.1, happy=0.3, sad=0.1,
                                               angry=0.1, fear=0.4)
        EmotionFromTextExtractionResult.objects.create(user=self.user, datetime=yesterday, surprise=0.1, joy=0.3,
                                                       sadness=0.1, anger=0.1, fear=0.4) # to check if it's not returned
        EmotionExtractionResult.objects.create(user=self.user, neutral=0.3, happy=0.55, sad=0.5, angry=0.07, fear=0.03)
        EmotionExtractionResult.objects.create(user=self.user, neutral=0.6, happy=0.25, sad=0.5, angry=0.07, fear=0.03)
        self.assertEqual(EmotionExtractionResult.objects.count(), 3)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        voice_results = list(self.user.voice_results)
        self.assertEqual(len(voice_results), 2)
        self.assertEqual(list(voice_results[0].keys()), ['date', 'voice_happiness_level'])
        self.assertEqual(voice_results[0]['date'], yesterday.date().strftime(date_format))
        self.assertEqual(voice_results[1]['date'], now.date().strftime(date_format))
        self.assertEqual(voice_results[0]['voice_happiness_level'], 0.3)
        self.assertEqual(voice_results[1]['voice_happiness_level'], 0.4)

    def test_user_text_results(self):
        now = datetime.utcnow()
        yesterday = now - timedelta(days=1)
        date_format = '%d-%m-%Y'
        EmotionFromTextExtractionResult.objects.create(user=self.user, datetime=yesterday, surprise=0.1, joy=0.3,
                                                       sadness=0.1, anger=0.1, fear=0.4)
        EmotionExtractionResult.objects.create(user=self.user, datetime=yesterday, neutral=0.1, happy=0.3, sad=0.1,
                                               angry=0.1, fear=0.4) # to check if it's not returned
        EmotionFromTextExtractionResult.objects.create(user=self.user, surprise=0.3, joy=0.55, sadness=0.5, anger=0.07,
                                                       fear=0.03)
        EmotionFromTextExtractionResult.objects.create(user=self.user, surprise=0.6, joy=0.25, sadness=0.5, anger=0.07,
                                                       fear=0.03)
        self.assertEqual(EmotionFromTextExtractionResult.objects.count(), 3)
        self.assertEqual(HappinessLevel.objects.count(), 2)
        text_results = list(self.user.text_results)
        self.assertEqual(len(text_results), 2)
        self.assertEqual(list(text_results[0].keys()), ['date', 'text_happiness_level'])
        self.assertEqual(text_results[0]['date'], yesterday.date().strftime(date_format))
        self.assertEqual(text_results[1]['date'], now.date().strftime(date_format))
        self.assertEqual(text_results[0]['text_happiness_level'], 0.3)
        self.assertEqual(text_results[1]['text_happiness_level'], 0.4)
