import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  kotlinMultiplatform
  kotlinNativeCocoapods
  androidLib
  kotlinxSerialization
  kotlinKapt
  daggerHiltAndroid
  mokoKSwift
  googleKsp
  buildKonfig
}

version = appConfig.versionName

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
      isStatic = true

      export(deps.coroutines.core)
      export(deps.napier)
      export(deps.kmpViewModel.core)
      export(deps.kmpViewModel.savedState)
    }
  }

  sourceSets {
    all {
      languageSettings.run {
        optIn("kotlinx.coroutines.FlowPreview")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        languageVersion = "1.9"
        progressiveMode = true
        enableLanguageFeature("DataObjects")
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(project(":flowredux"))

        // Kmp-ViewModel
        api(deps.kmpViewModel.core)
        api(deps.kmpViewModel.savedState)

        // Flow, Coroutines
        api(deps.coroutines.core)
        implementation(deps.flowExt)

        // Arrow-kt
        api(deps.arrow.core)
        implementation(deps.arrow.fx)

        // Serialization
        implementation(deps.serialization.core)
        implementation(deps.serialization.json)

        // Ktor
        implementation(deps.ktor.core)
        implementation(deps.ktor.clientJson)
        implementation(deps.ktor.serializationKotlinXJson)
        implementation(deps.ktor.negotiation)
        implementation(deps.ktor.logging)
        implementation(deps.ktor.serialization)

        // Logger
        api(deps.napier)

        // Kotlinx libs
        api(deps.dateTime)
        api(deps.atomicfu)
        api(deps.immutableCollections)

        implementation(deps.mokoKSwiftRuntime)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))

        implementation(deps.coroutines.test)
        implementation(deps.test.turbine)
        implementation(deps.ktor.mock)
        implementation(deps.test.mockative)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(deps.ktor.okHttp)
        implementation(deps.dagger.hiltAndroid)
        implementation(deps.compose.runtime)
        implementation(platform(deps.compose.bom))
      }
    }
    val androidTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-junit"))
        implementation(deps.test.junit)
      }
    }

    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)

      dependencies {
        implementation(deps.koin.core)
        implementation(deps.ktor.darwin)
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
  namespace = "com.hoc081098.github_search_kmm"
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

    // Sets Java compatibility to Java 11
    sourceCompatibility = VERSION_11
    targetCompatibility = VERSION_11
  }

  dependencies {
    coreLibraryDesugaring(deps.desugarJdkLibs)
    "kapt"(deps.dagger.hiltAndroidCompiler)
  }

  testOptions {
    unitTests {
      isReturnDefaultValues = true
      all {
        if (it.name == "testDebugUnitTest") {
          it.extensions.configure<kotlinx.kover.api.KoverTaskExtension> {
            isDisabled.set(false)
            // excludes.addAll(excludedClasses)
          }
        }
      }
    }
  }
}

hilt {
  enableAggregatingTask = true
}

kswift {
  install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature) {
    filter = dev.icerock.moko.kswift.plugin.feature.Filter.Include(emptySet())
  }
}

tasks.withType<KotlinNativeLink>()
  .matching { it.binary is Framework }
  .configureEach {
    doLast {
      val kSwiftGeneratedDir = destinationDirectory.get()
        .dir("${binary.baseName}Swift")
        .asFile

      val kSwiftPodSourceDir = buildDir
        .resolve("cocoapods")
        .resolve("framework")
        .resolve("${binary.baseName}Swift")

      kSwiftGeneratedDir.copyRecursively(kSwiftPodSourceDir, overwrite = true)
      println("[COPIED] $kSwiftGeneratedDir -> $kSwiftPodSourceDir")
    }
  }

dependencies {
  configurations
    .filter { it.name.startsWith("ksp") && it.name.contains("Test") }
    .forEach {
      add(it.name, deps.test.mockativeProcessor)
    }
}

buildkonfig {
  packageName = "com.hoc081098.github_search_kmm"
  defaultConfigs {
    buildConfigField(BOOLEAN, "IS_CI_BUILD", isCiBuild.toString())
  }
}

kover {
  instrumentation {
    excludeTasks += "testReleaseUnitTest" // exclude testReleaseUnitTest from instrumentation
  }
}

tasks.register<Copy>("copyiOSTestResources") {
  from("src/commonTest/resources")
  into("build/bin/iosX64/debugTest/resources")
}

tasks.findByName("iosX64Test")!!.dependsOn("copyiOSTestResources")
