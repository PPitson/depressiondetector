class MongoDocument:

    def __init__(self, **fields):
        self.__dict__.update(fields)


class User(MongoDocument):
    pass
