repositories {
    mavenCentral()
    jcenter()
    google()
    gradlePluginPortal()
}

plugins {
    kotlin("jvm")
    idea
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

application {
    mainClassName = "se.elite.dsc.kafka.taxi.trip.TripConverterMainKt"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.guava:guava:28.0-jre")
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    implementation("org.apache.kafka:kafka-streams:2.5.0")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-log4j12:1.7.25")
    implementation(project(":lib"))
}

