plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose") version "1.4.3"
}

allprojects {
  group = "ltd.mbor.minima.dapp"
  version = "1.0-SNAPSHOT"
  repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.github.com/mihbor/MinimaK") {
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }
}

kotlin {
  js(IR) {
    browser {
//            commonWebpackConfig{
//                devServer?.`open` = false
//            }
    }
    binaries.executable()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        implementation(compose.web.core)
        implementation(compose.runtime)
        implementation(project(":common"))
      }
    }
  }
}

tasks.register<Copy>("updateDappVersion") {
  from("src/jsMain/resources/dapp.conf")
  into(layout.buildDirectory.dir("distributions/"))
  filter { line -> line.replace("\"version\": \".*\"".toRegex(), "\"version\": \"$version\"") }
}

tasks.register<Copy>("copyService") {
  dependsOn(":service:jsBrowserDistribution")
  from("service/build/distributions/service.js")
  into(layout.buildDirectory.dir("processedResources/js/main/"))
}

tasks["jsBrowserDistribution"].dependsOn("updateDappVersion", "copyService")
tasks["jsProductionExecutableCompileSync"].dependsOn("copyService")
tasks["jsBrowserProductionExecutableDistributeResources"].dependsOn("copyService")

tasks.register<Zip>("minidappDistribution") {
  dependsOn("jsBrowserDistribution")
  archiveFileName.set("${project.name}-${project.version}.mds.zip")
  destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
  from(layout.buildDirectory.dir("distributions"))
}

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(1, "hours")
}
