plugins {
    kotlin("jvm") version "1.9.25"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.bytedeco:opencv-platform:4.10.0-1.5.11")
}

application {
    mainClass.set("AppKt")
}
