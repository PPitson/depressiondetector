from pymongo import MongoClient
import os


def get_collection(collection_name):
    client = MongoClient(os.getenv('MONGOLAB_URI'))
    db = client.depressiondata
    return db[collection_name]