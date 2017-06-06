import cloudconvert
import os


def convert(amr_file):
    api = cloudconvert.Api(os.environ['CLOUDCONVERT_API_KEY'])
    process = api.convert({
        'inputformat': 'amr',
        'outputformat': 'wav',
        'input': 'upload',
        'file': open(amr_file, 'rb')
    })
    process.wait()
    process.download()