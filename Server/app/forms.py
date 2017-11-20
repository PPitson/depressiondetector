from datetime import datetime

from flask_wtf import FlaskForm
from wtforms import PasswordField, SubmitField, DateTimeField, IntegerField
from wtforms.validators import DataRequired, EqualTo, NumberRange


class PasswordResetForm(FlaskForm):
    password = PasswordField('New Password', validators=[DataRequired(),
                                                         EqualTo('password2', message='Passwords must match')])
    password2 = PasswordField('Confirm password', validators=[DataRequired()])
    submit = SubmitField('Reset Password')


class MapDataForm(FlaskForm):
    format = '%Y-%m-%d %H:%M:%S'
    min_tweets = 1
    max_tweets = 1000
    default_tweets = 500
    date = DateTimeField('Date (format = {})'.format(format), validators=[DataRequired('Format: {}'.format(format))],
                         format=format, default=datetime.strptime('2017-11-16 20:00:00', format))
    max_tweets = IntegerField('Tweets (min = {}, max = {})'.format(min_tweets, max_tweets),
                              validators=[DataRequired(), NumberRange(min=min_tweets, max=max_tweets)],
                              default=default_tweets)
    submit = SubmitField('Show map')
