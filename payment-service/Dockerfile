FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
