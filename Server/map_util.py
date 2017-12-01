import math


def sent2r(s):
    return 255 if s < 0 else math.ceil((s - 1) * (-255))


def sent2g(s):
    return 255 if s > 0 else math.floor((s + 1) * 255)


def rgb2hex(r, g, b):
    return "#{:02x}{:02x}{:02x}".format(r, g, b)