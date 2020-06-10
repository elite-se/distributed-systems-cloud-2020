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
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.guava:guava:28.0-jre")
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-log4j12:1.7.25")
    implementation(project(":lib"))
}

