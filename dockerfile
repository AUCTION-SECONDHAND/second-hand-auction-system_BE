FROM openjdk:17

ARG FILE_JAR=target/second-hand-auction-system_BE-0.0.1-SNAPSHOT.jar

ADD ${FILE_JAR} /api-service.jar

ENTRYPOINT ["java", "-jar", "/api-service.jar"]

EXPOSE 8080
