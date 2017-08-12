def init_celery(app, celery):
    celery.conf.update(
        app.config,
        CELERY_ACCEPT_CONTENT=['pickle'],
        CELERY_TASK_SERIALIZER='pickle',
        CELERY_RESULT_SERIALIZER='pickle'
    )
    TaskBase = celery.Task

    class ContextTask(TaskBase):
        abstract = True

        def __call__(self, *args, **kwargs):
            with app.app_context():
                return super().__call__(*args, **kwargs)

    celery.Task = ContextTask
