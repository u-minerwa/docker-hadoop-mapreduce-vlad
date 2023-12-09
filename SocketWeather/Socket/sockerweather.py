import requests
import json 
import os
import pytz
import datetime
from io import StringIO
import time
import socket


CONST_MOSCOW = "Moscow"
CONST_EUROPE = "Europe"

common_city = "Saratov"
common_country = "RU"
country = "Russia"
s_city = common_city+','+common_country       # "Saratov,RU" 
city_id = 0
appid = "2a9391878d3c4a87279b49bdc5f73a9d"


# Создаем TCP/IP сокет
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Привязываем сокет к адресу и порту
server_socket.bind(('localhost', 9999))

# Слушаем входящие соединения
server_socket.listen(5)

print("Ждем подключения клиента...")

# Принимаем подключение
client_socket, address = server_socket.accept()

print(f"Подключен клиент с адресом: {address}")


while(True): 
    # Проверка наличия в базе информации о нужном населенном пункте: 
    try:
        res = requests.get("http://api.openweathermap.org/data/2.5/find", 
                        params={'q': s_city, 'type': 'like', 'units': 'metric', 'APPID': appid})
        data = res.json()
        cities = ["{} ({})".format(d['name'], d['sys']['country'])
                for d in data['list']]
        # print("city:", common_city)
        # print("country:", common_country)
        city_id = data['list'][0]['id']
        # print('city_id =', city_id)
    except Exception as e:
        # print("Exception (find):", e)
        pass


    # Получение информации о текущей погоде: 
    try:
        res = requests.get("http://api.openweathermap.org/data/2.5/weather", 
                        params={'id': city_id, 'units': 'metric', 'lang': 'en', 'APPID': appid})
        data = res.json()
        # print("conditions:", data['weather'][0]['description'])
        # print("temp:", data['main']['temp'])
        # print("temp_min:", data['main']['temp_min'])
        # print("temp_max:", data['main']['temp_max'])
    except Exception as e:
        # print("Exception (weather):", e)
        pass


    # Date Time: 
    tz_a = pytz.timezone(CONST_EUROPE+'/'+common_city)      # "Europe/Saratov"
    dt_a = datetime.datetime.now(tz_a)
    # print('\n')


    # Создание JSON формата: 
    weather_json_first = json.dumps({"city": common_city, "country": common_country, "city_id": city_id, 
                    "conditions": data['weather'][0]['description'], 
                    "date_time_now": str(dt_a), 
                    "temp": data['main']['temp'], 
                    "temp_min": data['main']['temp_min'], 
                    "temp_max": data['main']['temp_max']} ) + "\n"


    client_socket.send(weather_json_first.encode('utf-8'))

    print("Successful!")
    time.sleep(10)


# Закрываем соединение
client_socket.close()
server_socket.close()

 