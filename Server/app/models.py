from flask import current_app
from app import db
from werkzeug.security import check_password_hash, generate_password_hash
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired
import mongoengine as mongo

from datetime import datetime


class User(db.Document):

    username = mongo.StringField(max_length=25)
    email = mongo.EmailField(required=True)
    password_hash = mongo.StringField(required=True)
    sex = mongo.StringField(choices=('M', 'F'))
    date_of_birth = mongo.DateTimeField()

    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)

    def generate_reset_token(self, expiration=3600):
        serializer = Serializer(current_app.config['SECRET_KEY'], expiration)
        return serializer.dumps({'reset': self.username})

    def reset_password(self, token, new_password):
        serializer = Serializer(current_app.config['SECRET_KEY'])
        try:
            data = serializer.loads(token)
        except (BadSignature, SignatureExpired):
            return False
        if data.get('reset') != self.username:
            return False
        self.password_hash = generate_password_hash(new_password)
        self.save()
        return True


class EmotionExtractionResult(db.Document):

    username = mongo.StringField(max_length=25)
    datetime = mongo.DateTimeField(default=datetime.utcnow())
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()
