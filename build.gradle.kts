import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.process.internal.ExecException
import java.io.ByteArrayOutputStream
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "8.3.1"
    id("java")
    id("application")
}

application {
    mainClass = "com.github.yuqingliu.economy.Main"
}

group = "com.github.yuqingliu.economy"
version = "2.0.3-SNAPSHOT"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.property("paper_version")}")
    compileOnly("org.projectlombok:lombok:${project.property("lombok_version")}")

    annotationProcessor("org.projectlombok:lombok:${project.property("lombok_version")}")
    testCompileOnly("org.projectlombok:lombok:${project.property("lombok_version")}")
    testAnnotationProcessor("org.projectlombok:lombok:${project.property("lombok_version")}")

    implementation(project(":api"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("com.google.inject:guice:7.0.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
}

fun currentBranch(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        project.exec {
            commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
            standardOutput = stdout
        }

        val branchName = stdout.toString()
        branchName.trim().replace("/", "+")
    } catch (e: ExecException) {
        println("Error executing Git command: ${e.message}")
        "undefined"
    }
}

fun versionString(): String {
    val branchName: String = currentBranch()
    val releaseType: String = if (branchName == "master") "release" else "dev+${branchName}"
    return "$version".replace("-SNAPSHOT", "-$releaseType")
}

fun archiveName(): String {
    return "${project.name}-${versionString()}.jar"
}

tasks.register("printArchiveFileName") {
    doLast {
        println(archiveName())
    }
}

// Version Injection
tasks.processResources {
    val fullVersion: String = versionString()

    inputs.property("fullVersion", fullVersion)
    filesMatching("**/plugin.yml") {
        filter<ReplaceTokens>(
                "beginToken" to "\${",
                "endToken" to "}",
                "tokens" to mapOf(
                        "full.version" to fullVersion
                )
        )
    }

    println("Finished injecting version: $fullVersion")
}

tasks.test {
    useJUnitPlatform()
}

// Disable the default jar task
tasks.named<Jar>("jar") {
    isEnabled = false
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
