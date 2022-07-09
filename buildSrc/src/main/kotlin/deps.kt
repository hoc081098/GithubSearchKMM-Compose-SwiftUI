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
  const val kotlin = "1.6.21"
  const val agp = "7.2.1"
  const val gradleVersions = "0.42.0"
}

object appConfig {
  const val applicationId = "com.hoc081988.github_search_kmm.android"

  const val compileSdkVersion = 32
  const val buildToolsVersion = "32.0.0"

  const val minSdkVersion = 23
  const val targetSdkVersion = 32

  private const val MAJOR = 0
  private const val MINOR = 0
  private const val PATCH = 1
  const val versionCode = MAJOR * 10000 + MINOR * 100 + PATCH
  const val versionName = "$MAJOR.$MINOR.$PATCH-SNAPSHOT"
}

object deps {
  object androidx {
    const val appCompat = "androidx.appcompat:appcompat:1.4.1"
    const val coreKtx = "androidx.core:core-ktx:1.7.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.3"
    const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01"
    const val material = "com.google.android.material:material:1.6.0"
  }

  object lifecycle {
    private const val version = "2.4.0"

    const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version" // viewModelScope
    const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version" // lifecycleScope
    const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:$version"
  }

  object squareup {
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    const val converterMoshi = "com.squareup.retrofit2:converter-moshi:2.9.0"
    const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2"
    const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.12.0"
    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.7"
  }

  object coroutines {
    private const val version = "1.6.2"

    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
  }

  object serialization {
    private const val version = "1.3.3"
    const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
    const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:$version"
  }

  object ktor {
    private const val version = "2.0.3"
    const val core = "io.ktor:ktor-client-core:$version"
    const val clientJson = "io.ktor:ktor-client-json:$version"
    const val logging = "io.ktor:ktor-client-logging:$version"
    const val okHttp = "io.ktor:ktor-client-okhttp:$version"
    const val ios = "io.ktor:ktor-client-ios:$version"
    const val serialization = "io.ktor:ktor-client-serialization:$version"
    const val mock = "io.ktor:ktor-client-mock:$version"
    const val negotiation = "io.ktor:ktor-client-content-negotiation:$version"
    const val serializationKotlinXJson = "io.ktor:ktor-serialization-kotlinx-json:$version"
  }

  object koin {
    private const val version = "3.2.0"

    const val core = "io.insert-koin:koin-core:$version"
    const val android = "io.insert-koin:koin-android:$version"
    const val testJunit4 = "io.insert-koin:koin-test-junit4:$version"
    const val test = "io.insert-koin:koin-test:$version"
  }

  object dagger {
    const val version = "2.42"
    const val hiltAndroid = "com.google.dagger:hilt-android:$version"
    const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$version"
  }

  const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0"

  const val coil = "io.coil-kt:coil:2.0.0-rc03"
  const val viewBindingDelegate = "com.github.hoc081098:ViewBindingDelegate:1.3.1"
  const val flowExt = "io.github.hoc081098:FlowExt:0.4.0-SNAPSHOT"

  const val atomicfu = "org.jetbrains.kotlinx:atomicfu:0.18.2"

  object arrow {
    private const val version = "1.1.2"
    const val core = "io.arrow-kt:arrow-core:$version"
    const val fx = "io.arrow-kt:arrow-fx-coroutines:$version"
  }

  const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:1.1.5"
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

    const val mockk = "io.mockk:mockk:1.12.4"
    const val kotlinJUnit = "org.jetbrains.kotlin:kotlin-test-junit:${versions.kotlin}"
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

inline val DependencyHandler.shared get() = project(":shared")

fun DependencyHandler.addUnitTest(testImplementation: Boolean = true) {
  val configName = if (testImplementation) "testImplementation" else "implementation"

  add(configName, deps.test.junit)
  add(configName, deps.test.mockk)
  add(configName, deps.test.kotlinJUnit)
  add(configName, deps.coroutines.test)
}

val Project.isCiBuild: Boolean
  get() = providers.environmentVariable("CI").orNull == "true"

@Suppress("NOTHING_TO_INLINE")
object L {
  inline operator fun <T> get(vararg elements: T): List<T> = elements.asList()
}

@Suppress("NOTHING_TO_INLINE")
object M {
  inline operator fun <K, V> get(vararg elements: Pair<K, V>) = elements.toMap()
}
