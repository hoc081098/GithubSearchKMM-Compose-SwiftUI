plugins {
  androidApplication
  kotlinAndroid
  kotlinKapt
  kotlinParcelize
  daggerHiltAndroid
}

hilt {
  enableAggregatingTask = true
}

android {
  namespace = "com.hoc081098.github_search_kmm.android"
  compileSdk = appConfig.compileSdkVersion
  defaultConfig {
    applicationId = appConfig.applicationId
    minSdk = appConfig.minSdkVersion
    targetSdk = appConfig.targetSdkVersion
    versionCode = appConfig.versionCode
    versionName = appConfig.versionName
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    // Flag to enable support for the new language APIs
    // For AGP 4.1+
    isCoreLibraryDesugaringEnabled = true

    // Sets Java compatibility to Java 11
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  buildFeatures {
    // Enables Jetpack Compose for this module
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = deps.compose.androidxComposeCompilerVersion
  }

  kotlinOptions {
    freeCompilerArgs = freeCompilerArgs + buildComposeMetricsParameters() + listOf(
      "-opt-in=kotlin.RequiresOptIn",
      // Enable experimental coroutines APIs, including Flow
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-opt-in=kotlinx.coroutines.FlowPreview",
      "-opt-in=kotlin.Experimental",
      // Enable experimental kotlinx serialization APIs
      "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    )
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

  packagingOptions {
    resources {
      // TODO: Remove workaround for https://github.com/Kotlin/kotlinx.coroutines/issues/3668
      excludes += "META-INF/versions/9/previous-compilation-data.bin"
    }
  }
}

dependencies {
  implementation(shared)

  coreLibraryDesugaring(deps.desugarJdkLibs)

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)
  implementation(deps.androidx.activityCompose)
  implementation(deps.androidx.material)
  implementation(deps.androidx.hiltNavigationCompose)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)
  implementation(deps.lifecycle.runtimeCompose)

  implementation(deps.coroutines.core)
  implementation(deps.coroutines.android)

  implementation(deps.coilCompose)
  implementation(deps.flowExt)

  implementation(deps.dagger.hiltAndroid)
  kapt(deps.dagger.hiltAndroidCompiler)

  // Compose
  implementation(platform(deps.compose.bom))
  implementation(deps.compose.foundation)
  implementation(deps.compose.foundationLayout)
  implementation(deps.compose.materialIconsExtended)
  implementation(deps.compose.material3)
  debugImplementation(deps.compose.uiTooling)
  implementation(deps.compose.uiToolingPreview)
  implementation(deps.compose.uiUtil)
  implementation(deps.compose.runtime)
}

fun Project.buildComposeMetricsParameters(): List<String> {
  val metricParameters = mutableListOf<String>()
  val enableMetricsProvider = project.providers.gradleProperty("enableComposeCompilerMetrics")
  val enableMetrics = (enableMetricsProvider.orNull == "true")
  if (enableMetrics) {
    val metricsFolder = File(project.buildDir, "compose-metrics")
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + metricsFolder.absolutePath
    )
  }

  val enableReportsProvider = project.providers.gradleProperty("enableComposeCompilerReports")
  val enableReports = (enableReportsProvider.orNull == "true")
  if (enableReports) {
    val reportsFolder = File(project.buildDir, "compose-reports")
    metricParameters.add("-P")
    metricParameters.add(
      "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + reportsFolder.absolutePath
    )
  }
  return metricParameters.toList()
}

kover {
  instrumentation {
    excludeTasks += "testReleaseUnitTest" // exclude testReleaseUnitTest from instrumentation
  }
}
