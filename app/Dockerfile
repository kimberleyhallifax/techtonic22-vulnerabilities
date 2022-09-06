FROM maven:3-jdk-8-slim AS builder

COPY src/ src/
COPY pom.xml pom.xml

RUN mvn clean package


FROM openjdk:8u181-slim-stretch
RUN apt-get update && apt-get install -y netcat

COPY --from=builder target/hello.jar hello.jar

ENTRYPOINT [ "java", "-jar", "hello.jar" ]

EXPOSE 8080
