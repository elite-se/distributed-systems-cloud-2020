FROM gradle:6.5.0-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build :taxi-trip-converter:shadowJar --no-daemon

FROM openjdk:8-jre-slim

COPY --from=build /home/gradle/src/taxi-trip-converter/build/libs/taxi-trip-converter-1.0.0-all.jar /app/converter.jar

CMD java -jar /app/converter.jar
