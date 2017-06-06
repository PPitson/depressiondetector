from subprocess import call


def convert(amr_file):
    name, _ = amr_file.split('.')
    call(f'ffmpeg -i {amr_file} {name}.wav', shell=True)
