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
    }
  }

  sourceSets {
    all {
      languageSettings.run {
        optIn("kotlinx.coroutines.FlowPreview")
        optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
      }
    }

    val commonMain by getting {
      dependencies {
        implementation(project(":flowredux"))
        api(project(":multiplatform-viewmodel"))

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
        implementation(kotlin("test"))
        implementation(deps.ktor.mock)
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(deps.ktor.okHttp)
        implementation(deps.dagger.hiltAndroid)
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
}

hilt {
  enableAggregatingTask = true
}

kswift {
  install(dev.icerock.moko.kswift.plugin.feature.SealedToSwiftEnumFeature)
}

tasks.withType<KotlinNativeLink>()
  .matching { it.binary is Framework }
  .configureEach {
    doLast {
      val swiftDirectory = destinationDirectory.get()
        .dir("${binary.baseName}Swift")
        .asFile
      val xcodeSwiftDirectory = File(buildDir, "generated/swift")
      swiftDirectory.copyRecursively(xcodeSwiftDirectory, overwrite = true)
    }
  }
