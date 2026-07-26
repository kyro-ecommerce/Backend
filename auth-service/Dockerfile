FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn install -N

COPY auth-service ./auth-service
RUN mvn clean package -f auth-service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY --from=builder /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
