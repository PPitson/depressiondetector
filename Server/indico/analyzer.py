import indicoio

from app.models import EmotionFromTextExtractionResult
from indico.translator import translate


def analyze_text(text_bytes, user):
    original = text_bytes.decode("utf-8")
    text = translate(original, src='pl', dest='en').text
    print(text)
    emotions = indicoio.emotion(text)
    if emotions:
        result = EmotionFromTextExtractionResult(user=user, **emotions)
        result.save()
