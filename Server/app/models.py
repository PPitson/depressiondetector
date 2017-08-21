from flask import current_app
from app import db
import app.exceptions as exceptions
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

    @property
    def password(self):
        raise AttributeError('password is not a readable attribute')

    @password.setter
    def password(self, password):
        self.password_hash = generate_password_hash(password)

    @property
    def emotion_extraction_results(self):
        return EmotionExtractionResult.objects.filter(user=self).exclude('id').all()

    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)

    def generate_token(self, expiration=3600):
        serializer = Serializer(current_app.config['SECRET_KEY'], expiration)
        return serializer.dumps({'username': self.username})

    @staticmethod
    def load_user_from_token(token):
        serializer = Serializer(current_app.config['SECRET_KEY'])
        try:
            data = serializer.loads(token)
        except (BadSignature, SignatureExpired):
            return None
        username = data.get('username')
        return User.objects.filter(username=username).first()


class EmotionExtractionResult(db.Document):

    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    datetime = mongo.DateTimeField(default=datetime.utcnow())
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()
