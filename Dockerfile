FROM eclipse-temurin:17-jdk
WORKDIR /app
RUN mkdir ./data
# Copie du JAR généré par Maven
COPY target/*.jar app.jar
# On ne met pas d'ENTRYPOINT ici pour laisser le "command" du docker-compose diriger