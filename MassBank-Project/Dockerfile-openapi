FROM maven:3.8-jdk-11 as build
COPY ./MassBank-lib/ /build/MassBank-lib/
COPY ./MassBank-OpenAPI/pom.xml /build/MassBank-OpenAPI/pom.xml
COPY ./MassBank-web /build/MassBank-web/
COPY ./pom.xml /build/
COPY ./.mvn/* /build/.mvn/
WORKDIR /build/
RUN mvn verify -pl MassBank-OpenAPI --fail-never --settings ./.mvn/local-settings.xml
ADD ./MassBank-OpenAPI/ /build/MassBank-OpenAPI/
RUN mvn package -pl MassBank-OpenAPI -am --settings ./.mvn/local-settings.xml

FROM openjdk:11-jdk-slim
ARG JAR_FILE=/build/MassBank-OpenAPI/target/*.jar
COPY --from=build ${JAR_FILE} /
EXPOSE 8080/tcp
ENTRYPOINT java -jar *.jar