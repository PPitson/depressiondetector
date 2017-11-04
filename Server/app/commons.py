from flask import request

from app import exceptions


def get_json_or_raise_exception():
    request_json = request.get_json()
    if not request_json:
        raise exceptions.JSONMissingException
    return request_json


def get_json_list_or_raise_exception():
    request_json = request.get_json()
    if not request_json or not isinstance(request_json, list):
        raise exceptions.JSONListMissingException
    return request_json
