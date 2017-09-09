from flask import current_app
from app import db
from werkzeug.security import check_password_hash, generate_password_hash
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired
import mongoengine as mongo

from datetime import datetime
import json


class User(db.Document):

    username = mongo.StringField(max_length=25, required=True)
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
        return EmotionExtractionResult.objects.filter(user=self).all()

    def verify_password(self, password):
        return check_password_hash(self.password_hash, password)

    def generate_token(self, expiration=3600):
        serializer = Serializer(current_app.config['SECRET_KEY'], expiration)
        return serializer.dumps({'pk': str(self.pk)})

    @staticmethod
    def load_user_from_token(token):
        serializer = Serializer(current_app.config['SECRET_KEY'])
        try:
            data = serializer.loads(token)
        except (BadSignature, SignatureExpired):
            return None
        primary_key = data.get('pk')
        return User.objects.filter(pk=primary_key).first()

    def to_json(self):
        return json.loads(super().to_json())

    @staticmethod
    def get_fields():
        fields = set(field for field in User._fields_ordered if field != 'id')
        fields.remove('password_hash')
        fields.add('password')
        return fields


class EmotionExtractionResult(db.Document):

    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    datetime = mongo.DateTimeField(default=datetime.utcnow())
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()

    def to_json(self):
        return json.loads(super().to_json())
