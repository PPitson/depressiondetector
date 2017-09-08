from app import celery, create_app
from app.celery.factory import init_celery

app = create_app()
init_celery(app, celery)
