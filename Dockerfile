FROM maven:3-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -P -dev

#Run tage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/second-hand-auction-system_BE-0.0.1-SNAPSHOT api-service.jar
EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "pay-os.jar"]
ENTRYPOINT ["java", "-jar", "api-service.jar"]
#ARG FILE_JAR=target/eScentedCandle-0.0.1-SNAPSHOT.jar
#
#ADD ${FILE_JAR} api-service.jar
#
#ENTRYPOINT ["java", "-jar", "api-service.jar"]
#
#EXPOSE 8081