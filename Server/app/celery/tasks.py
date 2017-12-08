import os

import requests

from app import celery
from app.models import Tweet
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


@celery.task(name='get_tweet_markers')
def get_tweet_markers(start_datetime, end_datetime, tweets):
    markers = []
    for tweet in Tweet.objects.filter(created_at__gte=start_datetime, created_at__lt=end_datetime)[0:tweets]:
        coord = tweet.coordinates['coordinates']
        markers.append({
            'icon': get_icon_by_sentiment(tweet.sentiment),
            'lat': coord[1],
            'lng': coord[0],
            'infobox': tweet.text
        })
    return markers


def get_icon_by_sentiment(sentiment):
    base_dir = os.path.join('static', 'images')
    color = 'red'
    if sentiment > 0.5:
        color = 'green'
    elif sentiment > 0:
        color = 'yellow'
    elif sentiment > -0.5:
        color = 'purple'
    return os.path.join(base_dir, color + '.png')
