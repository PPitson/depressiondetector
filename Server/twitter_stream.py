import os

import numpy as np
import tweepy

import runcelery  # to initalize app and celery instances
from app.celery.tasks import analyze_and_save_tweet
from app.models import Tweet

ACCESS_TOKEN = os.environ['TWITTER_ACCESS_TOKEN']
ACCESS_TOKEN_SECRET = os.environ['TWITTER_ACCESS_TOKEN_SECRET']
CONSUMER_KEY = os.environ['TWITTER_CONSUMER_KEY']
CONSUMER_SECRET = os.environ['TWITTER_CONSUMER_SECRET']
GEOBOX_WORLD = [-180, -90, 180, 90]


def get_coordinates(status):
    return status.coordinates if status.coordinates else place_to_coordinates(status.place)


def place_to_coordinates(place):
    south_west_lon, south_west_lat = place.bounding_box.origin()
    north_east_lon, north_east_lat = place.bounding_box.corner()
    return [np.random.uniform(south_west_lon, north_east_lon), np.random.uniform(south_west_lat, north_east_lat)]


class MyStreamListener(tweepy.StreamListener):
    def on_status(self, status):
        coordinates = get_coordinates(status)
        if len(status.text) < 100:
            return
        tweet = Tweet(id=status.id, created_at=status.created_at, coordinates=coordinates, text=status.text)
        analyze_and_save_tweet.delay(tweet)

    def on_error(self, status_code):
        if status_code == 420:
            return False


def get_twitter_api():
    auth = tweepy.OAuthHandler(CONSUMER_KEY, CONSUMER_SECRET)
    auth.set_access_token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET)
    return tweepy.API(auth)


def create_stream():
    api = get_twitter_api()
    stream_listener = MyStreamListener()
    return tweepy.Stream(auth=api.auth, listener=stream_listener)


if __name__ == '__main__':
    stream = create_stream()
    stream.filter(locations=GEOBOX_WORLD, languages=['en'])
