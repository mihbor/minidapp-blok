plugins {
    kotlin("multiplatform") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0-alpha4-build362"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

kotlin {
    js(IR) {
        browser{
            commonWebpackConfig{
                devServer?.`open` = false
            }
        }
        binaries.executable()
    }
    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
            }
        }
    }
}
afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
        versions.webpackCli.version = "4.9.0"
    }
}
tasks.register<Zip>("minidappDistribution") {
    dependsOn("jsBrowserDistribution")
    archiveFileName.set("blok.minidapp")
    destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
    from(layout.buildDirectory.dir("distributions")) {
        exclude("*.map")
    }
}