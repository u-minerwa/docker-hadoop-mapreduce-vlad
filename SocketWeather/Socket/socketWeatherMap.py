import socket

# Создаем TCP/IP сокет
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Привязываем сокет к адресу и порту
server_socket.bind(('127.0.0.1', 12345))

# Слушаем входящие соединения
server_socket.listen(5)

print("Ждем подключения клиента...")

# Принимаем подключение
client_socket, address = server_socket.accept()

print(f"Подключен клиент с адресом: {address}")

# Отправляем ответ клиенту
response = "Привет, клиент!"
client_socket.send(response.encode('utf-8'))

# Закрываем соединение
client_socket.close()
server_socket.close()

