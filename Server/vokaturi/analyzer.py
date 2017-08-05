import sys
import os
from .api import Vokaturi
import scipy.io.wavfile


def get_system_and_architecture():
    system = sys.platform
    if system == 'linux2':
        system = 'linux'
    elif system == 'darwin':
        system = 'mac'
    elif system == 'win32':
        system = 'win'
    architecture = 64 if sys.maxsize > 2 ** 32 else 32
    return system, architecture


def analyze_file(filename):
    system, architecture = get_system_and_architecture()
    extension = 'dll' if system == 'win' else 'so'
    lib_file = f'Vokaturi_{system}{architecture}.{extension}'

    lib_dir = os.path.join(os.path.dirname(__file__), 'lib')
    Vokaturi.load(os.path.join(lib_dir, lib_file))

    (sample_rate, samples) = scipy.io.wavfile.read(filename)

    buffer_length = len(samples)
    c_buffer = Vokaturi.SampleArrayC(buffer_length)
    if samples.ndim == 1:  # mono
        c_buffer[:] = samples[:] / 32768.0
    else:  # stereo
        c_buffer[:] = 0.5 * (samples[:, 0] + 0.0 + samples[:, 1]) / 32768.0

    voice = Vokaturi.Voice(sample_rate, buffer_length)
    voice.fill(buffer_length, c_buffer)
    quality = Vokaturi.Quality()
    emotion_probabilities = Vokaturi.EmotionProbabilities()
    voice.extract(quality, emotion_probabilities)
    voice.destroy()

    if quality.valid:
        return {
            'neutral': emotion_probabilities.neutrality,
            'happy': emotion_probabilities.happiness,
            'sad': emotion_probabilities.sadness,
            'angry': emotion_probabilities.anger,
            'fear': emotion_probabilities.fear
        }
    return {}