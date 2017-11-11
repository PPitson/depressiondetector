import indicoio

from app.models import EmotionFromTextExtractionResult
from indico.translator import translate


def analyze_text(original_message, datetime, user):
    translated_message = translate(original_message, dest='en').text
    emotions = indicoio.emotion(translated_message)
    if emotions:
        return EmotionFromTextExtractionResult(user=user, datetime=datetime, **emotions)
