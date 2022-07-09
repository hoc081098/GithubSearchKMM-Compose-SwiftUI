import org.gradle.api.JavaVersion.VERSION_11

plugins {
  kotlin("multiplatform")
  kotlin("native.cocoapods")
  id("com.android.library")
  id("kotlinx-serialization")
}

version = "1.0"

kotlin {
  android()
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  cocoapods {
    summary = "Some description for the Shared Module"
    homepage = "Link to the Shared Module homepage"
    ios.deploymentTarget = "14.1"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "shared"
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(deps.coroutines.core)
        implementation(deps.flowExt)

        api(deps.arrow.core)
        implementation(deps.arrow.fx)

        implementation(deps.serialization.core)
        implementation(deps.serialization.json)

        implementation(deps.ktor.core)
        implementation(deps.ktor.json)
        implementation(deps.ktor.logging)
        implementation(deps.ktor.serialization)

        implementation(deps.napier)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(deps.ktor.mock)
      }
    }
    val androidMain by getting {
      dependencies {
        implementation(deps.ktor.okHttp)
      }
    }
    val androidTest by getting
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)

      dependencies {
        implementation(deps.ktor.ios)
        implementation(deps.ktor.core)
      }
    }
    val iosX64Test by getting
    val iosArm64Test by getting
    val iosSimulatorArm64Test by getting
    val iosTest by creating {
      dependsOn(commonTest)
      iosX64Test.dependsOn(this)
      iosArm64Test.dependsOn(this)
      iosSimulatorArm64Test.dependsOn(this)
    }
  }
}

android {
  compileSdk = appConfig.compileSdkVersion
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  defaultConfig {
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion
  }

  compileOptions {
    // Flag to enable support for the new language APIs

    // For AGP 4.1+
    isCoreLibraryDesugaringEnabled = true

    // Sets Java compatibility to Java 8
    sourceCompatibility = VERSION_11
    targetCompatibility = VERSION_11
  }
}
