import os
import time

import numpy as np
import tweepy

import twitter.config as conf
from app.celery.tasks import analyze_and_save_tweet
from app.models import Tweet, User
from twitter import logger


def get_coordinates(status):
    return status.coordinates if status.coordinates else place_to_coordinates(status.place)


def place_to_coordinates(place):
    south_west_lon, south_west_lat = place.bounding_box.origin()
    north_east_lon, north_east_lat = place.bounding_box.corner()
    return [np.random.uniform(south_west_lon, north_east_lon), np.random.uniform(south_west_lat, north_east_lat)]


class MyStreamListener(tweepy.StreamListener):
    last_timestamp = time.time()
    count = 0
    max_tweets_per_minute = int(os.environ.get('TWEETS_PER_MINUTE', -1))  # -1 -> no limit

    def on_status(self, status):
        if time.time() - self.last_timestamp > 60:
            logger.info(f'Tweets in last minute: {self.count}')
            self.last_timestamp = time.time()
            self.count = 0
        elif self.count == self.max_tweets_per_minute:
            return

        coordinates = get_coordinates(status)
        if len(status.text) < 100:
            return
        tweet = Tweet(id=status.id, datetime=status.created_at, coordinates=coordinates)
        analyze_and_save_tweet.delay(tweet, status.text)
        self.count += 1

    def on_error(self, status_code):
        if status_code == 420:
            logger.error('Rate limit error')
            time.sleep(30)
            return False


def get_twitter_api():
    auth = tweepy.OAuthHandler(conf.CONSUMER_KEY, conf.CONSUMER_SECRET)
    auth.set_access_token(conf.ACCESS_TOKEN, conf.ACCESS_TOKEN_SECRET)
    return tweepy.API(auth)


def create_stream():
    api = get_twitter_api()
    stream_listener = MyStreamListener()
    return tweepy.Stream(auth=api.auth, listener=stream_listener)


def run():
    while True:
        stream = create_stream()
        try:
            stream.filter(locations=conf.GEOBOX_WORLD, languages=['en'])
        except Exception as e:
            logger.error(e.args[0])
            time.sleep(60)


if __name__ == '__main__':
    run()
