# выход при любой ошибке
set -e


# Сначала соберём приложение
jc build


# Структура базы
./app-run.sh db-create -drop


# Тестовые данные
./app-run.sh db-loadtestdata -s base


# Кубы
./app-run.sh cube-db-init
./app-run.sh cube-interval -all -dbeg "2024-01-01" -dend "2024-12-31"
./app-run.sh cube-audit -all

