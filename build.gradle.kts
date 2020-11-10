import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinTest
import org.gradle.api.tasks.testing.Test

plugins {
    kotlin("jvm") version "1.4.10"
    application
    `java-library`
}

apply(plugin = "java-library")

group = "edu.mcgill"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.blueanvil:kotka:1.1.1")
    implementation("org.apache.kafka:kafka-streams:2.6.0")
    implementation("es.upm.etsisi:cf4j:2.1.1")
    implementation("com.github.holgerbrandl:krangl:-SNAPSHOT")
    implementation("com.beust:klaxon:5.0.1")
    implementation("org.apache.commons:commons-csv:1.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
