
import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
  kotlinMultiplatform
  id("org.jetbrains.kotlinx.kover")
}

kotlin {
  explicitApi()

  jvm {
    compilations.all {
      kotlinOptions.jvmTarget = VERSION_11.toString()
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
    val jvmMain by getting {
      dependsOn(commonMain)
    }
    val jvmTest by getting {
      dependsOn(commonTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }
//    val jsMain by getting {
//      dependsOn(commonMain)
//    }
//    val jsTest by getting {
//      dependencies {
//        implementation(kotlin("test-js"))
//      }
//    }

    val nativeMain by creating {
      dependsOn(commonMain)
    }
    val nativeTest by creating {
      dependsOn(commonTest)
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

  // enable running ios tests on a background thread as well
  // configuration copied from: https://github.com/square/okio/pull/929
  targets.withType<KotlinNativeTargetWithTests<*>>().all {
    binaries {
      // Configure a separate test where code runs in background
      test("background", setOf(NativeBuildType.DEBUG)) {
        freeCompilerArgs = freeCompilerArgs + "-trw"
      }
    }
    testRuns {
      val background by creating {
        setExecutionSourceFrom(binaries.getTest("background", NativeBuildType.DEBUG))
      }
    }
  }
}
