FROM openjdk:17-jdk-slim

ARG JAR_FILE=/build/libs/board-kopring-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} /board-kopring.jar

ENTRYPOINT ["java", "-jar","-Dspring.profiles.active=local", "/board-kopring.jar"]
