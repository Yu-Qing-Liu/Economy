plugins {
    id("java")
}

group = "com.github.yuqingliu.economy.api"
version = parent!!.version

repositories {
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven ("https://jitpack.io")
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.parent?.property("paper_version")}")
    compileOnly("org.projectlombok:lombok:${project.property("lombok_version")}")
    annotationProcessor("org.projectlombok:lombok:${project.property("lombok_version")}")
}

tasks.test {
    useJUnitPlatform()
}
