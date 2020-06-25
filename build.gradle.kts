allprojects {
    group = "se.elite.dsc"
    version = "1.0.0"

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    kotlin("jvm") version "1.3.72"
    idea
}

repositories {
    mavenCentral()
    jcenter()
    google()
    gradlePluginPortal()
}
