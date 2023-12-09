FROM python:latest

RUN pip install requests
RUN pip install pytz

WORKDIR /
ADD /Socket /Socket 
ADD /start.sh /start.sh

RUN ls
CMD ./start.sh
