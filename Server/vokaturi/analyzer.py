import os
import sys
import uuid

from app.models import EmotionExtractionResult
from converter.amr2wav import convert
from vokaturi.api.Vokaturi import extract_emotions


def get_system_and_architecture():
    platform_to_name_dict = {'darwin': 'mac', 'win32': 'win'}
    system = platform_to_name_dict.get(sys.platform, 'linux')
    architecture = 64 if sys.maxsize > 2 ** 32 else 32
    return system, architecture


def create_lib_path(system, architecture):
    extension = 'dll' if system == 'win' else 'so'
    lib_file = f'Vokaturi_{system}{architecture}.{extension}'
    lib_dir = os.path.join(os.path.dirname(__file__), 'lib')
    return os.path.join(lib_dir, lib_file)


def analyze_file(file_bytes, datetime, user):
    filename = uuid.uuid4().hex
    amr_filename, wav_filename = f'{filename}.amr', f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(file_bytes)
    convert(amr_filename)
    system, architecture = get_system_and_architecture()
    lib_path = create_lib_path(system, architecture)
    emotions = extract_emotions(wav_filename, lib_path)
    os.remove(amr_filename)
    os.remove(wav_filename)
    if emotions:
        return EmotionExtractionResult(user=user, datetime=datetime, **emotions)
