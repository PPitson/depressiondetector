from googletrans import Translator


def translate(original, dest):
    translator = Translator()
    return translator.translate(original, dest=dest)
