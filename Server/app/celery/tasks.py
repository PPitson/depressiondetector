import os
from collections import defaultdict
from datetime import datetime, timedelta

import geohash
import numpy as np
import requests
from textblob import TextBlob

from app import celery
from app.models import Tweet, GeoSentiment
from indico.analyzer import analyze_text
from vokaturi.analyzer import analyze_file


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, datetime, user, coordinates):
    document = analyze_file(file_bytes, datetime, user, coordinates)
    if document:
        save_result.delay(document)


@celery.task(name='analyze_text')
def analyze_text_task(msg, datetime, user, coordinates):
    document = analyze_text(msg, datetime, user, coordinates)
    if document:
        save_result.delay(document)


@celery.task(name='save_result')
def save_result(document):
    document.save()


@celery.task(name='wake_up')
def wake_up():
    requests.get('https://depressionserver.herokuapp.com')


@celery.task(name='analyze_and_save_tweet')
def analyze_and_save_tweet(tweet, text):
    sentiment = TextBlob(text).sentiment.polarity
    tweet.sentiment = sentiment
    lon, lat = tweet.coordinates['coordinates']
    tweet.geohash = geohash.encode(lat, lon, precision=3)
    tweet.save()


@celery.task(name='delete_obsolete_geosentiment')
def delete_obsolete_geosentiment():
    max_age = timedelta(days=int(os.environ.get('MAX_GEOSENTIMENT_AGE', 30)))
    max_datetime = datetime.now().date() - max_age
    GeoSentiment.objects(date__lte=max_datetime).delete()


@celery.task(name='count_mean_sentiment')
def count_mean_sentiment():
    today = datetime.now().date()
    start_datetime = today - timedelta(days=1)
    sentiments = defaultdict(lambda: [])
    for tweet in Tweet.objects(created_at__gte=start_datetime, created_at__lt=today):
        sentiments[tweet.geohash].append(tweet.sentiment)
    for ghash, sentiment_list in sentiments.items():
        mean_sentiment = np.mean(sentiment_list)
        GeoSentiment(geohash=ghash, date=start_datetime, mean_sentiment=mean_sentiment).save()
    delete_obsolete_tweets.delay()


@celery.task(name='delete_obsolete_tweets')
def delete_obsolete_tweets():
    max_datetime = datetime.now().date()
    Tweet.objects(created_at__lt=max_datetime).delete()
