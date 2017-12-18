import json
from abc import abstractmethod
from datetime import datetime, timedelta

import mongoengine as mongo
import numpy as np
from flask import current_app
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired
from werkzeug.security import check_password_hash, generate_password_hash

from app import db

MEAN_HAPPINESS_LEVEL_FIELD_PREFIX = 'mean'


class MongoDocument(db.Document):
    meta = {'allow_inheritance': True, 'abstract': True}

    def to_json(self):
        return json.loads(super().to_json())

    def __repr__(self):
        return repr(self.to_mongo())


class User(MongoDocument):
    username = mongo.StringField(max_length=25, required=True)
    email = mongo.EmailField(required=True)
    password_hash = mongo.StringField(required=True)
    sex = mongo.StringField(choices=('M', 'F'))
    date_of_birth: datetime = mongo.DateTimeField()
    is_admin = mongo.BooleanField(default=False)
    contact_person_email = mongo.EmailField()
    contact_person_phone = mongo.StringField()

    @property
    def password(self):
        raise AttributeError('password is not a readable attribute')

    @password.setter
    def password(self, password):
        self.password_hash = generate_password_hash(password)

    @property
    def voice_results(self):
        return HappinessLevel.get_results(user=self, data_source=EmotionExtractionResult.data_source)

    @property
    def text_results(self):
        return HappinessLevel.get_results(user=self, data_source=EmotionFromTextExtractionResult.data_source)

    @property
    def mood_results(self):
        return HappinessLevel.get_results(user=self, data_source=Mood.data_source)

    @property
    def mean_results(self):
        return HappinessLevel.get_results(user=self)

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

    @staticmethod
    def get_fields():
        fields = set(field for field in User._fields_ordered if field != 'id')
        fields.remove('password_hash')
        fields.add('password')
        return fields

    def to_json(self):
        result = {
            'username': self.username,
            'email': self.email,
        }
        if self.sex:
            result['sex'] = self.sex
        if self.date_of_birth:
            result['date_of_birth'] = self.date_of_birth.date().strftime('%d-%m-%Y')
        return result


class DataSourceMongoDocument(MongoDocument):
    meta = {'allow_inheritance': True, 'abstract': True}

    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    datetime: datetime = mongo.DateTimeField(default=datetime.utcnow)

    @abstractmethod
    def compute_happiness_level(self):
        pass

    def save(self, **kwargs):
        super().save(**kwargs)
        start_date = self.datetime.date()
        end_date = start_date + timedelta(days=1)
        happiness_levels = [emotions_result.compute_happiness_level() for emotions_result
                            in self.__class__.objects(user=self.user, datetime__gte=start_date, datetime__lt=end_date)]
        average = np.mean(happiness_levels)
        try:
            happiness = HappinessLevel.objects.get(user=self.user, date__gte=start_date, date__lt=end_date)
        except mongo.DoesNotExist:
            happiness = HappinessLevel(user=self.user, date=start_date)
        setattr(happiness, f'{self.data_source}_happiness_level', average)

        happiness.compute_mean()


class EmotionExtractionResult(DataSourceMongoDocument):
    data_source = 'voice'
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()

    def compute_happiness_level(self):
        return self.happy


class EmotionFromTextExtractionResult(DataSourceMongoDocument):
    data_source = 'text'
    anger = mongo.FloatField()
    joy = mongo.FloatField()
    fear = mongo.FloatField()
    sadness = mongo.FloatField()
    surprise = mongo.FloatField()

    def compute_happiness_level(self):
        return self.joy


class Mood(DataSourceMongoDocument):
    data_source = 'mood'
    mood_level: int = mongo.IntField(min_value=1, max_value=5)

    def compute_happiness_level(self):
        return (self.mood_level - 1) / 4


class HappinessLevel(MongoDocument):
    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    date = mongo.DateTimeField(default=datetime.utcnow().date())
    voice_happiness_level = mongo.FloatField()
    text_happiness_level = mongo.FloatField()
    mood_happiness_level = mongo.FloatField()
    mean_happiness_level = mongo.FloatField()

    @staticmethod
    def get_results(user, data_source=MEAN_HAPPINESS_LEVEL_FIELD_PREFIX):
        assert data_source in data_sources + (MEAN_HAPPINESS_LEVEL_FIELD_PREFIX,)
        field_name = f'{data_source}_happiness_level'
        return [{'date': result.date.strftime('%d-%m-%Y'), field_name: getattr(result, field_name)}
                for result in HappinessLevel.objects.filter(user=user).all() if getattr(result, field_name) is not None]

    def compute_mean(self):
        results = [getattr(self, f'{data_source}_happiness_level') for data_source in data_sources
                   if getattr(self, f'{data_source}_happiness_level') is not None]
        setattr(self, f'{MEAN_HAPPINESS_LEVEL_FIELD_PREFIX}_happiness_level', np.mean(results))
        self.save()


class Tweet(MongoDocument):
    meta = {
        'indexes': ['created_at'],
        'index_cls': False
    }

    id = mongo.LongField(primary_key=True)
    created_at = mongo.DateTimeField()
    coordinates = mongo.PointField()
    geohash = mongo.StringField()
    sentiment = mongo.FloatField(min_value=-1, max_value=1)


class GeoSentiment(MongoDocument):
    meta = {
        'indexes': ['date'],
        'index_cls': False
    }

    geohash = mongo.StringField()
    mean_sentiment = mongo.FloatField(min_value=-1, max_value=1)
    date = mongo.DateTimeField()


data_sources = (EmotionFromTextExtractionResult.data_source, EmotionExtractionResult.data_source,
                Mood.data_source)
