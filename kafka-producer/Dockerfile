FROM golang:latest
WORKDIR /

ADD /kafka-producer-application /kpa
ADD /start.sh /start.sh

WORKDIR /kpa/internal

RUN go build main.go
RUN mv main ../../main

RUN apt-get update -y
RUN apt-get install -y iputils-ping

WORKDIR /

CMD ./start.sh