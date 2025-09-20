plugins {
    kotlin("jvm") version "1.9.25"
    application
    id("org.jlleitschuh.gradle.ktlint") version "11.5.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.bytedeco:opencv-platform:4.10.0-1.5.11")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
}

application {
    mainClass.set("AppKt")
}

tasks.register("fmt") {
    group = "formatting"
    description = "Format Kotlin code with ktlint"
    dependsOn("ktlintFormat")
}

tasks.test {
    useJUnitPlatform()
}
