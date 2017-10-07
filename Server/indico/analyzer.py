import indicoio

from app.models import EmotionFromTextExtractionResult
from indico.translator import translate


def analyze_text(original, user):
    translated = translate(original,  dest='en').text
    emotions = indicoio.emotion(translated)
    if emotions:
        result = EmotionFromTextExtractionResult(user=user, **emotions)
        result.save()
