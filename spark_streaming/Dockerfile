FROM golang:latest
WORKDIR /

ADD /tcpserver /tcpserver
ADD /start.sh /start.sh

WORKDIR /tcpserver/internal

RUN go build main.go
RUN mv main ../../main

RUN apt-get update -y
RUN apt-get install -y iputils-ping

WORKDIR /

CMD ./start.sh