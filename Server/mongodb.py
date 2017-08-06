from pymongo import MongoClient
import os


def get_db():
    client = MongoClient(os.getenv('MONGOLAB_URI'))
    db = client.depressiondata
    return db
