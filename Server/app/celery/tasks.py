from app import celery
from vokaturi.analyzer import analyze_file


@celery.task(name='analyze_file')
def analyze_file_task(file_bytes, user):
    analyze_file(file_bytes, user)
