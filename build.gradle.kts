plugins {
    kotlin("jvm") version "1.9.24"
    id("java-library")
    id("maven-publish")
}

group = "com.kylecorry"
version = "9.4.0"

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
    maven{
        url = uri("https://jitpack.io")
        content {
            includeGroupByRegex("com\\.github.*")
        }
    }
    mavenCentral()
}

dependencies {
    implementation("com.github.kylecorry31:Geo-Coordinate-Conversion-Java:master")
    implementation("com.github.kylecorry31:osgb:v1.0.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}