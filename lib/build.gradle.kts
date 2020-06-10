plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.6")
}

repositories {
    mavenCentral()
}
