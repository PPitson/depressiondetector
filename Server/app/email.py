from flask import current_app, render_template
from flask_mail import Message
from app import mail
from threading import Thread


def send_email(recipents, subject, template, **kwargs):
    app = current_app._get_current_object()
    msg = Message(recipients=[recipents], subject=subject, sender=app.config['MAIL_USERNAME'])
    msg.body = render_template(template + '.txt', **kwargs)
    msg.html = render_template(template + '.html', **kwargs)
    Thread(target=send_email_async, args=(app, msg)).start()


def send_email_async(app, msg):
    with app.app_context():
        mail.send(msg)