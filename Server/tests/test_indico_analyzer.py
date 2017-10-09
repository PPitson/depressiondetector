from mock import patch, Mock

from app.models import EmotionFromTextExtractionResult
from indico.analyzer import analyze_text
from tests.testcase import CustomTestCase

EMOTIONS_DICT = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}


class IndicoioAnalyzerTestCase(CustomTestCase):
    @patch('indico.analyzer.indicoio.emotion', Mock(return_value=EMOTIONS_DICT))
    @patch('indico.analyzer.translate', Mock())
    def test_saves_results_after_successful_analysis(self):
        analyze_text('123', self.user)
        self.assertEqual(EmotionFromTextExtractionResult.objects.count(), 1)
        emotion_result = EmotionFromTextExtractionResult.objects.first()
        self.assertEqual(emotion_result.user, self.user)

    @patch('indico.analyzer.indicoio.emotion', Mock(return_value={}))
    @patch('indico.analyzer.translate', Mock())
    def test_doesnt_save_results_after_unsuccessful_analysis(self):
        analyze_text('123', self.user)
        self.assertEqual(EmotionFromTextExtractionResult.objects.count(), 0)
