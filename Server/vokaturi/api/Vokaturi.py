# Vokaturi.py
# Copyright (C) 2016 Paul Boersma, Johnny Ip, Toni Gojani
# version 2017-01-02

# This file is the Python interface to the Vokaturi library.
# The declarations are parallel to those in Vokaturi.h.

import ctypes
import scipy.io.wavfile

from typing import Dict


class Quality(ctypes.Structure):
    _fields_ = [
        ("valid", ctypes.c_int),
        ("num_frames_analyzed", ctypes.c_int),
        ("num_frames_lost", ctypes.c_int)]


class EmotionProbabilities(ctypes.Structure):
    _fields_ = [
        ("neutrality", ctypes.c_double),
        ("happiness", ctypes.c_double),
        ("sadness", ctypes.c_double),
        ("anger", ctypes.c_double),
        ("fear", ctypes.c_double)]


_library = None


def load(path_to_Vokaturi_library):
    global _library

    _library = ctypes.CDLL(path_to_Vokaturi_library)

    _library.VokaturiVoice_create.restype = ctypes.c_void_p
    _library.VokaturiVoice_create.argtypes = [
        ctypes.c_double,  # sample_rate
        ctypes.c_int]  # buffer_length

    _library.VokaturiVoice_setRelativePriorProbabilities.restype = None
    _library.VokaturiVoice_setRelativePriorProbabilities.argtypes = [
        ctypes.c_void_p,  # voice
        ctypes.POINTER(EmotionProbabilities)]  # priorEmotionProbabilities

    _library.VokaturiVoice_fill.restype = None
    _library.VokaturiVoice_fill.argtypes = [
        ctypes.c_void_p,  # voice
        ctypes.c_int,  # num_samples
        ctypes.POINTER(ctypes.c_double)]  # samples

    _library.VokaturiVoice_extract.restype = None
    _library.VokaturiVoice_extract.argtypes = [
        ctypes.c_void_p,  # voice
        ctypes.POINTER(Quality),  # quality
        ctypes.POINTER(EmotionProbabilities)]  # emotionProbabilities

    _library.VokaturiVoice_reset.restype = None
    _library.VokaturiVoice_reset.argtypes = [
        ctypes.c_void_p]  # voice

    _library.VokaturiVoice_destroy.restype = None
    _library.VokaturiVoice_destroy.argtypes = [
        ctypes.c_void_p]  # voice

    _library.Vokaturi_versionAndLicense.restype = ctypes.c_char_p
    _library.Vokaturi_versionAndLicense.argtypes = []


class Voice:
    def __init__(self, sample_rate, buffer_length):
        self._voice = _library.VokaturiVoice_create(sample_rate, buffer_length)

    def setRelativePriorProbabilities(self, priorEmotionProbabilities):
        _library.VokaturiVoice_setRelativePriorProbabilities(self._voice, priorEmotionProbabilities)

    def fill(self, num_samples, samples):
        _library.VokaturiVoice_fill(self._voice, num_samples, samples)

    def extract(self, quality, emotionProbabilities):
        _library.VokaturiVoice_extract(self._voice, quality, emotionProbabilities)

    def reset(self):
        _library.VokaturiVoice_reset(self._voice)

    def destroy(self):
        if _library is not None:
            _library.VokaturiVoice_destroy(self._voice)


def versionAndLicense():
    return _library.Vokaturi_versionAndLicense().decode("UTF-8")


def SampleArrayC(size):
    return (ctypes.c_double * size)()


def extract_emotions(filename, lib_path) -> Dict[str, float]:
    load(lib_path)
    (sample_rate, samples) = scipy.io.wavfile.read(filename)

    buffer_length = len(samples)
    c_buffer = SampleArrayC(buffer_length)
    if samples.ndim == 1:  # mono
        c_buffer[:] = samples[:] / 32768.0
    else:  # stereo
        c_buffer[:] = 0.5 * (samples[:, 0] + 0.0 + samples[:, 1]) / 32768.0

    voice = Voice(sample_rate, buffer_length)
    voice.fill(buffer_length, c_buffer)
    quality = Quality()
    emotion_probabilities = EmotionProbabilities()
    voice.extract(quality, emotion_probabilities)
    voice.destroy()

    if not quality.valid:
        return {}

    return {
        'neutral': emotion_probabilities.neutrality,
        'happy': emotion_probabilities.happiness,
        'sad': emotion_probabilities.sadness,
        'angry': emotion_probabilities.anger,
        'fear': emotion_probabilities.fear
    }
