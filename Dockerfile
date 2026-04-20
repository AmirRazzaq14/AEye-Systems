# Railway: when a Dockerfile exists, Railpack/mise is skipped — avoids BuildKit "secret api: not found".

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -q -Pproduction package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/AEye-Systems-1.0-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
