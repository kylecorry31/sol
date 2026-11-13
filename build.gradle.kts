import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.4.21"
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.37.0"
    id("dev.detekt") version "2.0.0-alpha.5"
}

val versionName = "19.4.0"

mavenPublishing {
    coordinates("com.kylecorry", "sol", versionName)

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

    publishToMavenCentral()
    signAllPublications()
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

// Setup javadocs
tasks.withType<Javadoc> {
    // Add the sources to the javadoc task
    source = sourceSets.main.get().allJava
    classpath += files(sourceSets.main.get().compileClasspath)
    // Only include public and protected classes
    include("**/public/**", "**/protected/**")
}

kotlin {
    jvmToolchain(11)
}
