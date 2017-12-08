from datetime import timedelta

from celery.schedules import crontab


def init_celery(app, celery):
    celery.conf.update(
        app.config,
        accept_content=['pickle'],
        task_serializer='pickle',
        result_serializer='pickle',
        task_routes={
            'save_result': {'queue': 'results_to_save'},
            'analyze_and_save_tweet': {'queue': 'twitter_stream'}
        },
        beat_schedule={
            'wake-up-every-30-minutes': {
                'task': 'wake_up',
                'schedule': timedelta(minutes=30),
            },
            'delete_obsolete_tweets': {
                'task': 'delete_obsolete_tweets',
                'schedule': crontab(minute=0, hour=0),
            },
            'count_mean_sentiment': {
                'task': 'count_mean_sentiment',
                'schedule': crontab(minute=0, hour=0),
            }
        }
    )
    TaskBase = celery.Task

    class ContextTask(TaskBase):
        abstract = True

        def __call__(self, *args, **kwargs):
            with app.app_context():
                return super().__call__(*args, **kwargs)

    celery.Task = ContextTask
