FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY eventos-core eventos-core
COPY passagens passagens
COPY passageiros-service passageiros-service
COPY eureka-server eureka-server
ARG MODULE
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -pl ${MODULE} -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE
RUN groupadd --gid 10001 app && useradd --uid 10001 --gid app app && mkdir /logs && chown app:app /logs
COPY --from=build /workspace/${MODULE}/target/*.jar app.jar
USER 10001:10001
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
