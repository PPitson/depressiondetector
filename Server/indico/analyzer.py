import indicoio
from indico.translator import translate

from app.models import EmotionFromTextExtractionResult


def analyze_text(original_message, datetime, user):
    translated_message = translate(original_message, dest='en').text
    emotions = indicoio.emotion(translated_message)
    if emotions:
        result = EmotionFromTextExtractionResult(user=user, datetime=datetime, **emotions)
        result.save()
