from datetime import timedelta


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
            'wake-up-every-15-minutes': {
                'task': 'wake_up',
                'schedule': timedelta(minutes=15),
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
