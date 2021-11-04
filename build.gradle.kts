import org.jetbrains.intellij.tasks.PublishPluginTask

plugins {
    `kotlin-dsl`
    id("org.jetbrains.intellij") version "1.0"
    kotlin("jvm") version "1.4.10"
}


group = "io.github.mishkun"
version = "1.2.1"

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2020.1")
    plugins.set(listOf("IdeaVIM:0.61"))
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set("""
        Reduce minimum required plugin version
    """.trimIndent())
    sinceBuild.set("183")
    untilBuild.set("")
}

tasks.withType<org.jetbrains.intellij.tasks.RunPluginVerifierTask> {
    ideVersions.set(listOf("IIC-203.5784.10", "PY-183.5912.18"))
}
