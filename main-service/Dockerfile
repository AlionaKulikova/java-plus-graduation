FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/main-service*.jar /app/ewm-service.jar

CMD ["java", "-jar", "/app/ewm-service.jar"]