FROM gradle:8.7-jdk21 AS build
WORKDIR /app

COPY build.gradle.kts build.gradle.kts

COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]