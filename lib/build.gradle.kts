plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.kafka:kafka-clients:2.5.0")
}

repositories {
    mavenCentral()
}
