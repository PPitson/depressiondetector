from pymongo import MongoClient
import os


def get_collection(collection_name):
    client = MongoClient(os.getenv('MONGOLAB_URI', 'mongodb://inzynier:chelseareal@ds163681.mlab.com:63681/depressiondata'))
    db = client.depressiondata
    return db[collection_name]