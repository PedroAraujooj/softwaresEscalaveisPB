FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY eventos-core eventos-core
COPY passagens passagens
COPY passageiros-service passageiros-service
COPY eureka-server eureka-server
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
