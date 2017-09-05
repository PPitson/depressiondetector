import time

from tests.testcase import CustomTestCase
from app.models import User, EmotionExtractionResult


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
            password = self.user.password

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

    def test_user_voice_emotion_results(self):
        self.assertEqual(self.user.emotion_extraction_results.count(), 0)
        result = EmotionExtractionResult(user=self.user, neutral=0.1, happy=0.3, sad=0.1, angry=0.1, fear=0.4)
        result.save()
        other_result = EmotionExtractionResult(user=self.user, neutral=0.3, happy=0.55, sad=0.5, angry=0.07, fear=0.03)
        other_result.save()
        self.assertEqual(self.user.emotion_extraction_results.count(), 2)
        self.assertIn(result, list(self.user.emotion_extraction_results))
        self.assertIn(other_result, list(self.user.emotion_extraction_results))



