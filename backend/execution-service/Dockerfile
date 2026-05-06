FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install Docker CLI so the service can launch sandbox containers
RUN apk add --no-cache docker-cli

COPY --from=build /app/target/execution-service-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
