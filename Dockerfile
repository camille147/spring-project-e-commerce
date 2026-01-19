# Utilise une image légère de Java 17
FROM eclipse-temurin:17-jdk

# Crée un dossier pour votre appli dans le conteneur
WORKDIR /app

# Copie le fichier .jar généré par Maven/Gradle dans le conteneur
# Assurez-vous d'avoir fait un 'mvn package' avant !
COPY target/*.jar app.jar

# Expose le port 8080 (standard Spring Boot)
EXPOSE 8080

# Lance l'application
ENTRYPOINT ["java", "-jar", "app.jar"]