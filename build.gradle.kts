import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

group = "edu.mcgill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.blueanvil:kotka:1.1.1")
    implementation("org.apache.kafka:kafka-streams:2.6.0")
    implementation("es.upm.etsisi:cf4j:2.1.1")
    implementation("com.github.holgerbrandl:krangl:-SNAPSHOT")
    implementation("com.beust:klaxon:5.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}