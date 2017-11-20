import os
from datetime import datetime, timedelta

from textblob import TextBlob

from app.models import Tweet
from . import celery


@celery.task(name='analyze_and_save_tweet')
def analyze_and_save_tweet(tweet):
    sentiment = TextBlob(tweet.text).sentiment.polarity
    tweet.sentiment = sentiment
    tweet.save()
    print(tweet.id, tweet.created_at, tweet.sentiment)


@celery.task(name='delete_obsolete_tweets')
def delete_obsolete_tweets():
    max_age = timedelta(days=int(os.environ.get('MAX_TWEETS_AGE', 7)))
    max_datetime = datetime.now() - max_age
    Tweet.objects(datetime__lte=max_datetime).delete()
