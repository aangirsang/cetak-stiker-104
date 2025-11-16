plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "1.9.25"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "com.girsang"
version = "1.0.4"
description = "Aplikasi Cetak Stiker Rumah BUMN Batubara"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.openjfx:javafx-controls:21.0.4")
    implementation("org.openjfx:javafx-fxml:21.0.4")
    implementation("org.mindrot:jbcrypt:0.4")
}

javafx {
    version = "21.0.4"
    modules = listOf("javafx.controls", "javafx.fxml")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}