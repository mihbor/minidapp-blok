rootProject.name = "minidapp-blok"

pluginManagement {
  val kotlinVersion = extra["kotlin.version"] as String
  repositories {
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
  plugins {
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
  }
}

include(":common", ":service")
