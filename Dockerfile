FROM openjdk:17-jdk-alpine

COPY data  /home/data

ADD mock-1.0.0.jar /home/app.jar

WORKDIR /home

EXPOSE 8990

CMD ["java", "-jar", "/home/app.jar"]