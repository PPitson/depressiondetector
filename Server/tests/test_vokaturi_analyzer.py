from unittest.mock import patch, Mock, call, mock_open
import os

from tests.testcase import CustomTestCase
from vokaturi.analyzer import analyze_file, get_system_and_architecture, create_lib_path
from app.models import EmotionExtractionResult

HEX = '1234567890ABCDEF'
EMOTIONS_DICT = {'happy': 0.5, 'sad': 0.07, 'angry': 0.03, 'fear': 0.3, 'neutral': 0.1}


@patch('vokaturi.analyzer.convert', Mock(return_value=''))
@patch('vokaturi.analyzer.open', mock_open())
class VokaturiAnalyzerTestCase(CustomTestCase):

    @patch('uuid.uuid4', Mock(return_value=Mock(hex=HEX)))
    @patch('vokaturi.analyzer.extract_emotions', Mock(return_value={}))
    @patch('vokaturi.analyzer.os.remove')
    def test_removes_files_after_analyzing(self, os_remove):
        analyze_file(b'123', self.user)
        amr_filename, wav_filename = f'{HEX}.amr', f'{HEX}.wav'
        self.assertEqual(os_remove.mock_calls, [call(amr_filename), call(wav_filename)])

    @patch('vokaturi.analyzer.extract_emotions', Mock(return_value=EMOTIONS_DICT))
    @patch('vokaturi.analyzer.os.remove', Mock())
    def test_saves_results_after_successful_analysis(self):
        analyze_file(b'123', self.user)
        self.assertEqual(EmotionExtractionResult.objects.count(), 1)
        emotion_result = EmotionExtractionResult.objects.first()
        self.assertEqual(emotion_result.user, self.user)

    @patch('vokaturi.analyzer.extract_emotions', Mock(return_value={}))
    @patch('vokaturi.analyzer.os.remove', Mock())
    def test_doesnt_save_results_after_unsuccessful_analysis(self):
        analyze_file(b'123', self.user)
        self.assertEqual(EmotionExtractionResult.objects.count(), 0)

    @patch('vokaturi.analyzer.sys')
    def test_get_system_and_architecture(self, sys):
        sys.platform = 'win32'
        sys.maxsize = 2 ** 32 + 1
        system, architecture = get_system_and_architecture()
        self.assertEqual(system, 'win')
        self.assertEqual(architecture, 64)

        sys.platform = 'darwin'
        sys.maxsize = 2 ** 32
        system, architecture = get_system_and_architecture()
        self.assertEqual(system, 'mac')
        self.assertEqual(architecture, 32)

        sys.platform = 'linux'
        sys.maxsize = 2 ** 32 - 1
        system, architecture = get_system_and_architecture()
        self.assertEqual(system, 'linux')
        self.assertEqual(architecture, 32)

    def test_create_lib_path(self):
        path = create_lib_path('win', 32)
        self.assertTrue(path.endswith(os.path.join('lib', os.path.sep, 'Vokaturi_win32.dll')), f'returned: {path}')
        path = create_lib_path('win', 64)
        self.assertTrue(path.endswith(os.path.join('lib', os.path.sep, 'Vokaturi_win64.dll')), f'returned: {path}')
        path = create_lib_path('mac', 32)
        self.assertTrue(path.endswith(os.path.join('lib', os.path.sep, 'Vokaturi_mac32.so')), f'returned: {path}')
        path = create_lib_path('linux', 64)
        self.assertTrue(path.endswith(os.path.join('lib', os.path.sep, 'Vokaturi_linux64.so')), f'returned: {path}')
