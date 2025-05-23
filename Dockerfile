FROM eclipse-temurin:24-jre

WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]