FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /workspace/target/*.jar habitat-api.jar
EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "habitat-api.jar" ]