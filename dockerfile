# syntax=docker/dockerfile:1

################################################################################

# Stage 1: Resolving and downloading dependencies
FROM eclipse-temurin:17-jdk-jammy as deps

WORKDIR /build

# Copy the mvnw wrapper with executable permissions
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

# Download dependencies as a separate step
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

################################################################################

# Stage 2: Build the application
FROM deps as package

WORKDIR /build

# Copy the source code
COPY ./src src/

# Build the application and create the JAR file
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################

# Stage 3: Extract Spring Boot layers
FROM package as extract

WORKDIR /build

# Extract Spring Boot application layers
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################

# Stage 4: Runtime
FROM eclipse-temurin:17-jre-jammy as final

WORKDIR /app

# Copy the extracted layers into the final image
COPY --from=extract /build/target/extracted/dependencies/ ./dependencies/
COPY --from=extract /build/target/extracted/spring-boot-loader/ ./spring-boot-loader/
COPY --from=extract /build/target/extracted/snapshot-dependencies/ ./snapshot-dependencies/
COPY --from=extract /build/target/extracted/application/ ./application/

# Expose the port the application will run on
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
