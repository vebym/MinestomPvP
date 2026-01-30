plugins {
    java
}

group = "io.github.togar2"
description = "Minecraft combat library for Minestom, with support for both 1.9+ and 1.8 combat"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenCentral()
}

val minestom = "2026.01.08-1.21.11"

dependencies {
    compileOnly("net.minestom:minestom:$minestom")
    testImplementation("net.minestom:minestom:$minestom")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}