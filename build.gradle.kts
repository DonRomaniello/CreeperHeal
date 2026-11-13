plugins {
    java
}

group = "com.nitnelave.CreeperHeal"
version = "9.0.0-paper"
description = "CreeperHeal handles explosions in a natural, but non-destructive way. Paper port of nitnelave's plugin."

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    // Compile with JDK 25 (fetched automatically if missing); the jar still targets Java 21.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.processResources {
    // plugin.yml carries the version as ${project.version}, same as the Maven build.
    val props = mapOf("project" to mapOf("version" to project.version.toString()))
    inputs.properties(mapOf("version" to project.version.toString()))
    filesMatching("plugin.yml") {
        expand(props)
    }
}
