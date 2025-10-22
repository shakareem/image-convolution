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
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.knowm.xchart:xchart:3.8.2")
    implementation("com.twelvemonkeys.imageio:imageio-bmp:3.9.4")
    implementation("com.twelvemonkeys.imageio:imageio-core:3.9.4")
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

tasks.register<JavaExec>("benchmark") {
    group = "benchmark"
    description = "Run image processing benchmark"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("BenchmarkKt")
}
