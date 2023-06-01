import hashlib
import os
import shutil
from pathlib import Path

# Кириллические имена файлов в md5


#
dir_base = "./"
dir_mp3 = dir_base + "mp3_grab/"
dir_mp3_md5 = dir_base + "mp3/"
if not os.path.exists(dir_mp3_md5):
    os.mkdir(dir_mp3_md5)

#
n = 0

files = Path(dir_mp3).glob('*.mp3')
for file_path in files:
    file_dir = os.path.dirname(file_path)
    file_name = os.path.basename(file_path)
    file_name_0 = os.path.splitext(file_name)[0]
    md5 = hashlib.md5(file_name_0.encode('utf-8')).hexdigest().lower()
    file_name_md5 = md5[0:16] + ".mp3"

    print(file_name + " -> " + file_name_md5)

    shutil.copy2(file_dir + "/" + file_name, dir_mp3_md5 + "/" + file_name_md5)
