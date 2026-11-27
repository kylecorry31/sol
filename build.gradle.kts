import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.21"
    id("java-library")
    id("maven-publish")
    id("dev.detekt") version "2.0.0-alpha.6"
}

val versionName = "19.5.0"

group = "com.kylecorry"
version = versionName

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = "sol"
            from(components["java"])

            pom {
                name.set("Sol")
                description.set("A Kotlin library for science and math in the real world.")
                url.set("https://github.com/kylecorry31/sol")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("kylecorry31")
                        name.set("Kyle Corry")
                        email.set("kylecorry31@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/kylecorry31/sol.git")
                    developerConnection.set("scm:git:ssh://github.com:kylecorry31/sol.git")
                    url.set("https://github.com/kylecorry31/sol")
                }
            }
        }
    }
}

repositories {
    maven {
        url = uri("https://jitpack.io")
        content {
            includeGroupByRegex("com\\.github.*")
        }
    }
    mavenCentral()
}

dependencies {
    detektPlugins("com.kylecorry:orion:1.1.0")
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// JUnit 6 requires Java 17, while the published library remains compatible with Java 11.
configurations.named("testCompileClasspath") {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
}

configurations.named("testRuntimeClasspath") {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
}

tasks.test {
    useJUnitPlatform()
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

tasks.named<JavaCompile>("compileTestJava") {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    baseline = file("$rootDir/config/detekt/baseline.xml")
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    setSource(files("src/main/kotlin"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
    exclude("**/gov/nasa/worldwind/**")
    classpath.setFrom(sourceSets.main.get().compileClasspath, sourceSets.main.get().output)
    jvmTarget = "11"
}

tasks.withType<dev.detekt.gradle.DetektCreateBaselineTask>().configureEach {
    setSource(files("src/main/kotlin"))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**")
    exclude("**/gov/nasa/worldwind/**")
    classpath.setFrom(sourceSets.main.get().compileClasspath, sourceSets.main.get().output)
    jvmTarget = "11"
}

kotlin {
    jvmToolchain(11)
}
