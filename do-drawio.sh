#!/bin/bash

# Пример: вызываем команду find для получения списка файлов и директорий
# Затем вызываем команду echo для каждой строки вывода find

# find . -name '*.drawio' 2>/dev/null

find . -name '*.drawio' 2>/dev/null | while read -r file; do
    # Вместо "echo" замените на вашу нужную команду
    echo "Обработка файла: $file"
    drawio -x -f png $file 2>/dev/null
done