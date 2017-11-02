from flask import current_app

from smsapi.client import SmsAPI
from smsapi.responses import ApiError


def send_sms(message, recipient):
    app = current_app._get_current_object()
    api = SmsAPI()
    api.auth_token = app.config['SMSAPI_TOKEN']
    try:
        api.service('sms').action('send')
        api.set_content(message)
        api.set_to(recipient)
        api.execute()
        return True
    except ApiError:
        return False
