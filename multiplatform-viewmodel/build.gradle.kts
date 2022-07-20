plugins {
  kotlinMultiplatform
  androidLib
}

kotlin {
  android()

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = org.gradle.api.JavaVersion.VERSION_11.toString()
    }
  }

//  js(BOTH) {
//    compilations.all {
//      kotlinOptions {
//        sourceMap = true
//        moduleKind = "umd"
//        metaInfo = true
//      }
//    }
//    browser {
//      testTask {
//        useMocha()
//      }
//    }
//    nodejs {
//      testTask {
//        useMocha()
//      }
//    }
//  }

  iosArm64()
  iosArm32()
  iosX64()
  iosSimulatorArm64()

  macosX64()
  macosArm64()
  mingwX64()
  linuxX64()

  tvosX64()
  tvosSimulatorArm64()
  tvosArm64()

  watchosArm32()
  watchosArm64()
  watchosX64()
  watchosX86()
  watchosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(deps.coroutines.core)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
        implementation(deps.coroutines.test)
        implementation(deps.test.turbine)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(deps.lifecycle.viewModelKtx)
      }
    }
    val androidTest by getting

    val nonAndroidMain by creating {
      dependsOn(commonMain)

      dependencies {
        implementation(deps.atomicfu)
      }
    }
    val nonAndroidTest by creating {
      dependsOn(commonTest)
    }

    val jvmMain by getting {
      dependsOn(nonAndroidMain)
    }
    val jvmTest by getting {
      dependsOn(nonAndroidTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

//    val jsMain by getting {
//      dependsOn(nonAndroidMain)
//    }
//    val jsTest by getting {
//      dependsOn(nonAndroidTest)
//      dependencies {
//        implementation(kotlin("test-js"))
//      }
//    }

    val nativeMain by creating {
      dependsOn(nonAndroidMain)
    }
    val nativeTest by creating {
      dependsOn(nonAndroidTest)
    }

    val appleTargets = listOf(
      "iosX64",
      "iosSimulatorArm64",
      "iosArm64",
      "iosArm32",
      "macosX64",
      "macosArm64",
      "tvosArm64",
      "tvosX64",
      "tvosSimulatorArm64",
      "watchosArm32",
      "watchosArm64",
      "watchosX86",
      "watchosSimulatorArm64",
      "watchosX64"
    )

    (appleTargets + listOf("mingwX64", "linuxX64")).forEach {
      getByName("${it}Main") {
        dependsOn(nativeMain)
      }
      getByName("${it}Test") {
        dependsOn(nativeTest)
      }
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
}
