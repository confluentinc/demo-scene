FROM openjdk:16

WORKDIR /code
COPY src ./src
COPY gradle ./gradle
COPY config ./config
COPY build.gradle gradlew gradlew.bat ./

ENV GRADLE_OPTS -Dorg.gradle.daemon=false
RUN ./gradlew build
