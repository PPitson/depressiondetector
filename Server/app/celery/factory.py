def init_celery(app, celery):
    celery.conf.update(
        app.config,
        accept_content=['pickle'],
        task_serializer='pickle',
        result_serializer='pickle',
        task_routes={'save_result': {'queue': 'results_to_save'}},
    )
    TaskBase = celery.Task

    class ContextTask(TaskBase):
        abstract = True

        def __call__(self, *args, **kwargs):
            with app.app_context():
                return super().__call__(*args, **kwargs)

    celery.Task = ContextTask
