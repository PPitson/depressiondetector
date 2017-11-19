import requests
from textblob import TextBlob

from app import celery
from indico.analyzer import analyze_text
from vokaturi.analyzer import analyze_file


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, datetime, user):
    document = analyze_file(file_bytes, datetime, user)
    if document:
        save_result.delay(document)


@celery.task(name='analyze_text')
def analyze_text_task(msg, datetime, user):
    document = analyze_text(msg, datetime, user)
    if document:
        save_result.delay(document)


@celery.task(name='save_result')
def save_result(document):
    document.save()


@celery.task(name='wake_up')
def wake_up():
    requests.get('https://depressionserver.herokuapp.com')


@celery.task(name='analyze_and_save_tweet')
def analyze_and_save_tweet(tweet):
    sentiment = TextBlob(tweet.text).sentiment.polarity
    tweet.sentiment = sentiment
    tweet.save()
    print(tweet.id, tweet.created_at, tweet.sentiment)
