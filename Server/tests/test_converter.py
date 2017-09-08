import unittest
from unittest.mock import patch

from converter.amr2wav import convert


class AMR2WAVConverter(unittest.TestCase):

    @patch('converter.amr2wav.call')
    def test_successful_call_to_ffmpeg(self, subprocess_call):
        convert('file.amr')
        subprocess_call.assert_called_with('ffmpeg -i file.amr file.wav', shell=True)
