from googletrans import Translator


def translate(original, src, dest):
    translator = Translator()
    return translator.translate(original, src=src, dest=dest)
