import math
from datetime import datetime, timedelta

import geohash

from app.models import GeoSentiment


def sent2r(s):
    return 255 if s < 0 else math.ceil((s - 1) * (-255))


def sent2g(s):
    return 255 if s > 0 else math.floor((s + 1) * 255)


def rgb2hex(r, g, b):
    return "#{:02x}{:02x}{:02x}".format(r, g, b)


def get_dates_by_slider(n, min, max):
    diff = max + min - n
    return datetime.now().date() - timedelta(days=diff), datetime.now().date() - timedelta(days=diff - 1)


def prepare_sentiment_rects(date_start, date_end):
    rects = []
    for geo_sentiment in GeoSentiment.objects(date__gte=date_start, date__lt=date_end):
        bounds = geohash.bbox(geo_sentiment.geohash)
        sentiment = geo_sentiment.mean_sentiment
        rects.append({
            'fill_color': rgb2hex(sent2r(s=sentiment), sent2g(s=sentiment), 0),
            'stroke_weight': 0,
            'fill_opacity': 0.5,
            'bounds': {
                'north': bounds['n'],
                'west': bounds['w'],
                'south': bounds['s'],
                'east': bounds['e']
            }
        })
    return rects
