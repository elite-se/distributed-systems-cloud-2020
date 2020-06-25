plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
    implementation("org.mongodb:mongodb-driver-sync:4.0.4")
    implementation("org.litote.kmongo:kmongo:4.0.2")
    implementation("org.apache.kafka:kafka-streams:2.0.0")
}

repositories {
    mavenCentral()
}