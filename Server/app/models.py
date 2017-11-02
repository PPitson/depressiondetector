from datetime import datetime, timedelta
import json

from flask import current_app
import mongoengine as mongo
import numpy as np
from werkzeug.security import check_password_hash, generate_password_hash
from itsdangerous import TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired
from abc import abstractmethod

from app import db


class MongoDocument(db.Document):

    meta = {'allow_inheritance': True}

    def to_json(self):
        return json.loads(super().to_json())

    def __repr__(self):
        return self.to_mongo()


class DataSourceMongoDocument(MongoDocument):

    meta = {'allow_inheritance': True}

    @abstractmethod
    def compute_happiness_level(self):
        pass

    def save(self, **kwargs):
        super().save(**kwargs)
        start_date = self.datetime.date()
        end_date = start_date + timedelta(days=1)
        happiness_levels = [emotions_result.compute_happiness_level() for emotions_result
                            in self.__class__.objects(datetime__gte=start_date, datetime__lt=end_date)]
        average = np.mean(happiness_levels)
        try:
            happiness = HappinessLevel.objects.get(user=self.user, date__gte=start_date, date__lt=end_date)
        except mongo.DoesNotExist:
            happiness = HappinessLevel(user=self.user, date=start_date)
        setattr(happiness, f'{self.data_source}_happiness_level', average)
        happiness.save()


class User(MongoDocument):

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
    def voice_results(self):
        return HappinessLevel.get_results_by_data_source(user=self, data_source=EmotionExtractionResult.data_source)

    @property
    def text_results(self):
        return HappinessLevel.get_results_by_data_source(user=self,
                                                         data_source=EmotionFromTextExtractionResult.data_source)

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


class EmotionExtractionResult(DataSourceMongoDocument):

    data_source = 'voice'
    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    datetime = mongo.DateTimeField(default=datetime.utcnow)
    neutral = mongo.FloatField()
    happy = mongo.FloatField()
    sad = mongo.FloatField()
    angry = mongo.FloatField()
    fear = mongo.FloatField()

    def compute_happiness_level(self):
        return self.happy


class EmotionFromTextExtractionResult(DataSourceMongoDocument):

    data_source = 'text'
    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    datetime = mongo.DateTimeField(default=datetime.utcnow)
    anger = mongo.FloatField()
    joy = mongo.FloatField()
    fear = mongo.FloatField()
    sadness = mongo.FloatField()
    surprise = mongo.FloatField()

    def compute_happiness_level(self):
        return self.joy


class HappinessLevel(MongoDocument):

    user = mongo.ReferenceField(User, reverse_delete_rule=mongo.CASCADE)
    date = mongo.DateTimeField(default=datetime.utcnow().date())
    voice_happiness_level = mongo.FloatField()
    text_happiness_level = mongo.FloatField()

    @staticmethod
    def get_results_by_data_source(user, data_source):
        assert data_source in (EmotionFromTextExtractionResult.data_source, EmotionExtractionResult.data_source)
        field_name = f'{data_source}_happiness_level'
        return [{'date': result.date.strftime('%d-%m-%Y'), field_name: getattr(result, field_name)}
                for result in HappinessLevel.objects.filter(user=user).all()]
