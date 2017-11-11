import dateutil.parser
from mock import patch, Mock

from app.models import EmotionFromTextExtractionResult
from indico.analyzer import analyze_text
from tests.testcase import CustomTestCase

EMOTIONS_DICT = {'joy': 0.5, 'sadness': 0.07, 'anger': 0.03, 'fear': 0.3, 'surprise': 0.1}


class IndicoioAnalyzerTestCase(CustomTestCase):
    datetime = dateutil.parser.parse('2008-10-14T20:56')

    @patch('indico.analyzer.indicoio.emotion', Mock(return_value=EMOTIONS_DICT))
    @patch('indico.analyzer.translate', Mock())
    def test_returns_results_after_successful_analysis(self):
        result = analyze_text('123', self.datetime, self.user)
        self.assertIsInstance(result, EmotionFromTextExtractionResult)

    @patch('indico.analyzer.indicoio.emotion', Mock(return_value={}))
    @patch('indico.analyzer.translate', Mock())
    def test_returns_nothing_after_unsuccessful_analysis(self):
        result = analyze_text('123', self.datetime, self.user)
        self.assertIsNone(result)