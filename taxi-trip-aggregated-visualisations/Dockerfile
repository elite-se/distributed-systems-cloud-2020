FROM gradle:6.5.0-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build :taxi-trip-aggregated-visualisations:shadowJar --no-daemon

FROM openjdk:8-jre-slim

COPY --from=build /home/gradle/src/taxi-trip-aggregated-visualisations/build/libs/taxi-trip-aggregated-visualisations-1.0.0-all.jar /app/aggregated-visualisations.jar

CMD java -jar /app/aggregated-visualisations.jar
