import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import tasks.TrainingTask

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
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("com.github.blueanvil:kotka:1.1.1")
    implementation("org.apache.kafka:kafka-streams:2.6.0")
    implementation("es.upm.etsisi:cf4j:2.1.1")
    implementation("com.github.holgerbrandl:krangl:-SNAPSHOT")
    implementation("com.beust:klaxon:5.0.1")
    implementation("org.apache.commons:commons-csv:1.8")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.11.0")
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

tasks.register<TrainingTask>("helloTask") {
    messageText = "Hello there!"
    webhookUrl = "your webhook URL here"
}

tasks.register("trainTask", JavaExec::class) {
    //findProperty("<ARGUMENT>")?.let { args = listOf(it.toString()) }
    main = "ModelTask"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("integrationTask", JavaExec::class) {
    //findProperty("<ARGUMENT>")?.let { args = listOf(it.toString()) }
    main = "IntegrationTask"
    classpath = sourceSets["main"].runtimeClasspath
}
