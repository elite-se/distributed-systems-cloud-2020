allprojects {
    group = "se.elite.dsc"
    version = "1.0.0"
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
