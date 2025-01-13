# Build stage
FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY . .

# Build ứng dụng và tạo file jar
RUN mvn clean package -DskipTests



# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Sao chép file jar từ bước build vào image
COPY --from=build /app/target/second-hand-auction-system_BE-0.0.1-SNAPSHOT.jar api-service.jar

# Expose cổng mà ứng dụng sẽ chạy trên container
EXPOSE 8080

# Khởi động ứng dụng khi container được chạy
ENTRYPOINT ["java", "-jar", "api-service.jar"]
