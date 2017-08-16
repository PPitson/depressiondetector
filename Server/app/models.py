from app import db
from werkzeug.security import check_password_hash
import mongoengine as mongo
from datetime import datetime


class User(db.Document):

    username = mongo.StringField(max_length=25)
    email = mongo.EmailField()
    password_hash = mongo.StringField()

    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)


class EmotionExtractionResult(db.Document):

    username = mongo.StringField(max_length=25)
    datetime = mongo.DateTimeField(default=datetime.utcnow())
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()
