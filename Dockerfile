# Giai đoạn 1: Build ứng dụng Spring Boot
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file pom.xml trước để tận dụng cache của Docker
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy toàn bộ source code còn lại
COPY src ./src

# Build ứng dụng, bỏ qua tests để build nhanh hơn
RUN mvn package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
# Sử dụng một image nhỏ gọn hơn chỉ chứa Java Runtime
FROM openjdk:21-slim
RUN apt-get update && apt-get install -y curl

WORKDIR /app

# Copy file .jar đã được build từ giai đoạn 1
COPY --from=build /app/target/*.jar app.jar

# Mở cổng 8080 để bên ngoài có thể truy cập vào
EXPOSE 8080

# Lệnh để khởi chạy ứng dụng khi container bắt đầu
ENTRYPOINT ["java", "-jar", "app.jar"]