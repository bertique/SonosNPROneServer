FROM ubuntu
MAINTAINER Michael Dick

WORKDIR /src

RUN apt-get update
RUN apt-get install openjdk-17-jdk --no-install-recommends -y
RUN apt-get install maven -y

ADD . /src

RUN mvn clean install package

EXPOSE $PORT

CMD mvn jetty:run -Djetty.http.port=$PORT
