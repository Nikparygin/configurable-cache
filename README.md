# configurable-cache
Конфигурируемый двух-уровневый кэш, реализующий LFU и LRU
----------------------------------------------------------
Запуск:
1) В терминале выполнить следующую команду:
> java -jar .\rest-service-1.0-SNAPSHOT.jar .\application.conf
2) Запустить батник run.bat

----------------------------------------------------------------------------------------------------------------------------------------------------

После запуска поднимится REST-сервис на localhost:8080 (по умолчанию)

Я добавил в проект сваггер, можно подергать методы через него:
http://localhost:8080/swagger-ui.html#/

Либо напрямую курлом:

Запись объекта в кэш:
curl -X POST "http://localhost:8080/cache/put?key=key1" -H "accept: */*" -H "Content-Type: application/json" -d "{\"custom_value1\":\"value1\"}"

Чтение по ключу:
curl -X GET "http://localhost:8080/cache/get/key1" -H "accept: */*"

Удаление по ключу:
curl -X DELETE "http://localhost:8080/cache/remove/key1" -H "accept: */*"

Очистка всего кэша:
curl -X DELETE "http://localhost:8080/cache/clear" -H "accept: */*"

Получение текущего размера кэша:
curl -X GET "http://localhost:8080/cache/size" -H "accept: */*"

курлы, как и батник - для запуска из-под windows

--------------------------------------------------------------------------------------------------------------------------------------------------

Исходники залил сюда:

https://github.com/Nikparygin/configurable-cache