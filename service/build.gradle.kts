plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}
base {
  archivesName.set("service")
}
kotlin {
  js(IR) {
    browser{
      webpackTask {
        sourceMaps = true
      }
    }
    binaries.executable()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(project(":common"))
      }
    }
  }
}
