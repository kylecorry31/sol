plugins {
    kotlin("jvm") version "1.9.10"
    id("java-library")
    id("maven-publish")
}

group = "com.kylecorry"
version = "7.1.5"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = group.toString()
                artifactId = "sol"
                version = version
                from(components["java"])
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.platform:junit-platform-runner:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.26.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}