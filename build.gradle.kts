plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("org.jetbrains.compose") version "1.6.0"
}

allprojects {
  group = "ltd.mbor.minima.dapp"
  version = "1.0-SNAPSHOT"
  repositories {
    mavenLocal()
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
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
        implementation(compose.html.core)
        implementation(compose.runtime)
        implementation(project(":common"))
      }
    }
  }
}

tasks.register<Copy>("updateDappVersion") {
  from("src/jsMain/resources/dapp.conf")
  into(layout.buildDirectory.dir("dist/js/productionExecutable/"))
  filter { line -> line.replace("\"version\": \".*\"".toRegex(), "\"version\": \"$version\"") }
}

tasks.register<Copy>("copyService") {
  dependsOn(":service:jsBrowserDistribution")
  from("service/build/dist/js/productionExecutable/service.js")
  into(layout.buildDirectory.dir("processedResources/js/main/"))
}

tasks["jsBrowserDistribution"].dependsOn("updateDappVersion", "copyService")
tasks["jsProductionExecutableCompileSync"].dependsOn("copyService")
tasks["jsBrowserProductionExecutableDistributeResources"].dependsOn("copyService")

tasks.register<Zip>("minidappDistribution") {
  dependsOn("jsBrowserDistribution")
  archiveFileName.set("${project.name}-${project.version}.mds.zip")
  destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
  from(layout.buildDirectory.dir("dist/js/productionExecutable"))
}
