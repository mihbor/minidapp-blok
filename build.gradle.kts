plugins {
    kotlin("multiplatform") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.compose") version "1.2.1"
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
//            commonWebpackConfig{
//                devServer?.`open` = false
//            }
        }
        binaries.executable()
    }
    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("com.ionspin.kotlin:bignum:0.3.7")
                implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:0.3.7")
            }
        }
    }
}
tasks.register<Zip>("minidappDistribution") {
    dependsOn("jsBrowserDistribution")
    archiveFileName.set("${project.name}.mds.zip")
    destinationDirectory.set(layout.buildDirectory.dir("minidapp"))
    from(layout.buildDirectory.dir("distributions"))
}