import org.jetbrains.intellij.tasks.PublishTask

plugins {
    id("org.jetbrains.intellij") version "0.6.2"
    kotlin("jvm") version "1.4.10"
}


group = "io.github.mishkun"
version = "1.0.4"

repositories {
    mavenCentral()
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.1"
    setPlugins("IdeaVIM:0.60")
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
        Set maximum compatible version to `open` to remove restrictions on nightly IDE versions
        
        This time, do it properly, kudos to @AlexPl292
      """)
    setSinceBuild("201")
    setUntilBuild("")
}

