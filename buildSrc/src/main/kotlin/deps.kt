@file:Suppress("unused", "ClassName", "SpellCheckingInspection")

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

object versions {
  const val spotless = "6.7.2"
  const val ktlint = "0.45.2"
  const val kotlin = "1.8.10"
  const val agp = "7.3.0"
  const val gradleVersions = "0.42.0"
  const val mokoKSwift = "0.6.1"
  const val googleKsp = "1.8.10-1.0.9"
  const val buildKonfig = "0.13.3"
}

object appConfig {
  const val applicationId = "com.hoc081098.github_search_kmm.android"

  const val compileSdkVersion = 33
  const val buildToolsVersion = "33.0.0"

  const val minSdkVersion = 23
  const val targetSdkVersion = 33

  private const val MAJOR = 0
  private const val MINOR = 0
  private const val PATCH = 2
  const val versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
  const val versionName = "$MAJOR.$MINOR.$PATCH-SNAPSHOT"
}

object deps {
  object androidx {
    const val appCompat = "androidx.appcompat:appcompat:1.6.1"
    const val coreKtx = "androidx.core:core-ktx:1.9.0"
    const val material = "com.google.android.material:material:1.8.0"
    const val activityCompose = "androidx.activity:activity-compose:1.7.0"
    const val hiltNavigationCompose = "androidx.hilt:hilt-navigation-compose:1.0.0"
  }

  object lifecycle {
    private const val version = "2.6.1"

    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version" // viewModelScope
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version" // lifecycleScope
    const val runtimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version" // lifecycleScope
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
  }

  object squareup {
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.12.0"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.9.1"
  }

  object coroutines {
    private const val version = "1.7.0-Beta"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
  }

  object serialization {
    private const val version = "1.4.0"
    const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
    const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:$version"
  }

  object ktor {
    private const val version = "2.2.4"
    const val core = "io.ktor:ktor-client-core:$version"
    const val clientJson = "io.ktor:ktor-client-json:$version"
    const val logging = "io.ktor:ktor-client-logging:$version"
    const val okHttp = "io.ktor:ktor-client-okhttp:$version"
    const val darwin = "io.ktor:ktor-client-darwin:$version"
    const val serialization = "io.ktor:ktor-client-serialization:$version"
    const val mock = "io.ktor:ktor-client-mock:$version"
    const val negotiation = "io.ktor:ktor-client-content-negotiation:$version"
    const val serializationKotlinXJson = "io.ktor:ktor-serialization-kotlinx-json:$version"
  }

  object compose {
    const val androidxComposeCompilerVersion = "1.4.4"
    const val bom = "androidx.compose:compose-bom:2023.04.00"

    const val foundation = "androidx.compose.foundation:foundation"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout"

    const val materialIconsExtended = "androidx.compose.material:material-icons-extended"
    const val material3 = "androidx.compose.material3:material3"
    const val material3WindowSizeClass = "androidx.compose.material3:material3-window-size-class"

    const val runtime = "androidx.compose.runtime:runtime"

    const val uiTooling = "androidx.compose.ui:ui-tooling"
    const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    const val uiUtil = "androidx.compose.ui:ui-util"
  }

  object koin {
    private const val version = "3.4.0"

    const val core = "io.insert-koin:koin-core:$version"
    const val testJunit4 = "io.insert-koin:koin-test-junit4:$version"
    const val test = "io.insert-koin:koin-test:$version"
  }

  object dagger {
    const val version = "2.45"
    const val hiltAndroid = "com.google.dagger:hilt-android:$version"
    const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$version"
  }

  object kmpViewModel {
    private const val version = "0.4.0"
    const val core = "io.github.hoc081098:kmp-viewmodel:$version"
    const val savedState = "io.github.hoc081098:kmp-viewmodel-savedstate:$version"
  }

  const val mokoKSwiftRuntime = "dev.icerock.moko:kswift-runtime:${versions.mokoKSwift}"
  const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"

  const val coilCompose = "io.coil-kt:coil-compose:2.3.0"
  const val flowExt = "io.github.hoc081098:FlowExt:0.6.0"

  const val atomicfu = "org.jetbrains.kotlinx:atomicfu:0.18.3"
  const val immutableCollections = "org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5"

  object arrow {
    private const val version = "1.2.0-RC"
    const val core = "io.arrow-kt:arrow-core:$version"
    const val fx = "io.arrow-kt:arrow-fx-coroutines:$version"
  }

  const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:1.2.2"
  const val napier = "io.github.aakira:napier:2.6.1"

  object test {
    const val junit = "junit:junit:4.13.2"

    object androidx {
      const val core = "androidx.test:core-ktx:1.4.0"
      const val junit = "androidx.test.ext:junit-ktx:1.1.3"

      object espresso {
        const val core = "androidx.test.espresso:espresso-core:3.4.0"
      }
    }

    const val mockative = "io.mockative:mockative:1.4.0"
    const val mockativeProcessor = "io.mockative:mockative-processor:1.4.0"
    const val turbine = "app.cash.turbine:turbine:0.12.3"
  }
}

private typealias PDsS = PluginDependenciesSpec
private typealias PDS = PluginDependencySpec

inline val PDsS.androidApplication: PDS get() = id("com.android.application")
inline val PDsS.androidLib: PDS get() = id("com.android.library")
inline val PDsS.kotlinAndroid: PDS get() = id("kotlin-android")
inline val PDsS.kotlin: PDS get() = id("kotlin")
inline val PDsS.kotlinKapt: PDS get() = id("kotlin-kapt")
inline val PDsS.kotlinParcelize: PDS get() = id("kotlin-parcelize")
inline val PDsS.kotlinxSerialization: PDS get() = id("kotlinx-serialization")
inline val PDsS.kotlinMultiplatform: PDS get() = kotlin("multiplatform")
inline val PDsS.kotlinNativeCocoapods: PDS get() = kotlin("native.cocoapods")
inline val PDsS.daggerHiltAndroid: PDS get() = id("dagger.hilt.android.plugin")
inline val PDsS.mokoKSwift: PDS get() = id("dev.icerock.moko.kswift")
inline val PDsS.googleKsp: PDS get() = id("com.google.devtools.ksp")
inline val PDsS.buildKonfig: PDS get() = id("com.codingfeline.buildkonfig")

inline val DependencyHandler.shared get() = project(":shared")
inline val DependencyHandler.flowRedux get() = project(":flowredux")
inline val DependencyHandler.multiplatformViewModel get() = project(":multiplatform-viewmodel")

val Project.isCiBuild: Boolean
  get() = providers.environmentVariable("CI").orNull == "true"

val excludedClasses = listOf(
  "hilt_aggregated_deps.*",
  "io.mockative.*"
)

@Suppress("NOTHING_TO_INLINE")
object L {
  inline operator fun <T> get(vararg elements: T): List<T> = elements.asList()
}

@Suppress("NOTHING_TO_INLINE")
object M {
  inline operator fun <K, V> get(vararg elements: Pair<K, V>) = elements.toMap()
}
