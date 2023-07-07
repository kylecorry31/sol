plugins {
    kotlin("jvm") version "1.8.20"
    id("java-library")
}

group = "com.kylecorry.sol"
version = "7.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-runner:1.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}