from app import celery
from indico.analyzer import analyze_text
from vokaturi.analyzer import analyze_file


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, user):
    analyze_file(file_bytes, user)


@celery.task(name='analyze_text')
def analyze_text_task(msg, datetime, user):
    analyze_text(msg, datetime, user)
