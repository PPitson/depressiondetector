import uuid
import os

from app.models import EmotionExtractionResult
from vokaturi.analyzer import extract_emotions
from converter.amr2wav import convert
from app import celery


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, user):
    filename = uuid.uuid4().hex
    amr_filename, wav_filename = f'{filename}.amr', f'{filename}.wav'
    with open(amr_filename, 'wb') as file:
        file.write(file_bytes)
    convert(amr_filename)
    emotions = extract_emotions(wav_filename)
    os.remove(amr_filename)
    os.remove(wav_filename)
    if emotions:
        result = EmotionExtractionResult(user=user, **emotions)
        result.save()
