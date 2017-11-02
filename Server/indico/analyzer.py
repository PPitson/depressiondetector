import indicoio
from indico.translator import translate

from app.models import EmotionFromTextExtractionResult


def analyze_text(original, user):
    translated = translate(original, dest='en').text
    emotions = indicoio.emotion(translated)
    if emotions:
        result = EmotionFromTextExtractionResult(user=user, **emotions)
        result.save()
