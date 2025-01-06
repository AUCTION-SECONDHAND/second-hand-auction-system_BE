# Build stage
FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/second-hand-auction-system_BE-0.0.1-SNAPSHOT.jar api-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "api-service.jar"]
