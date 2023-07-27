FROM --platform=linux/arm64 maven:3.8.4-jdk-11 AS build
WORKDIR /app
COPY . .
RUN mvn -f pom.xml -DskipTests package
FROM openjdk:11-jre-slim as deploy
COPY --from=build /app/target/*-execute.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","app.jar"]