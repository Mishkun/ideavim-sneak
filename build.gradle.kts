plugins {
    id("org.jetbrains.intellij") version "0.6.2"
    kotlin("jvm") version "1.4.10"
}


group = "io.github.mishkun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.2.2"
    setPlugins("IdeaVIM:0.60")
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
      Add change notes here.<br>
      <em>most HTML tags may be used</em>""")
}

