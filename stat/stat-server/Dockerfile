FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/stat-server*.jar /app/stat-server.jar
CMD ["java", "-jar", "/app/stat-server.jar"]