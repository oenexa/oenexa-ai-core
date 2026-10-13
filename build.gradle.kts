// Root build configuration for OENEXA AI Engineer
plugins {
    // Apply Kotlin DSL for Gradle
    `kotlin-dsl` version "2.0.20" apply false
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.spring") version "1.9.22" apply false
}

allprojects {
    group = "com.oenexa"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}
