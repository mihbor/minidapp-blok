plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}
kotlin {
  js(IR) {
    browser()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        api("ltd.mbor:minimak:0.4.2-SNAPSHOT")
      }
    }
  }
}
