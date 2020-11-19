import org.jetbrains.intellij.tasks.PublishTask

plugins {
    id("org.jetbrains.intellij") version "0.6.2"
    kotlin("jvm") version "1.4.10"
}


group = "io.github.mishkun"
version = "1.1.1"

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.1"
    setPlugins("IdeaVIM:0.61")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
}

tasks.withType<PublishTask> {
    val intellijPublishToken: String? by project
    token(intellijPublishToken)
}

tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes("""
        - Enable escape symbol for interrupting sneak movement
        - Add highlighting to found chars
        - Update IdeaVim to 0.61
      """)
    setSinceBuild("201")
    setUntilBuild("")
}

tasks.withType<org.jetbrains.intellij.tasks.RunPluginVerifierTask> {
    setIdeVersions("IIC-203.5784.10")
}
