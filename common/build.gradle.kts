val bignumVersion = "0.3.7"

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
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
        implementation("com.ionspin.kotlin:bignum:$bignumVersion")
        implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:$bignumVersion")

        api("ltd.mbor:minimak:0.4.2-SNAPSHOT")
      }
    }
  }
}
