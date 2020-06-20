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
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-log4j12:1.7.25")
    implementation(project(":lib"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.6.12")
}

