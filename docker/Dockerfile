FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew clean vaadinBuildFrontend bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV VAADIN_PRODUCTIONMODE=true

ENTRYPOINT ["java", "-jar", "app.jar"]