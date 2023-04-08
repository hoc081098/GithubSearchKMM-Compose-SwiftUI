import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.util.EnumSet
import kotlinx.kover.KoverPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}")
    classpath(kotlin("serialization", version = versions.kotlin))
    classpath("org.jetbrains.kotlinx:kover:0.6.1")
    classpath("com.android.tools.build:gradle:${versions.agp}")
    classpath("com.google.dagger:hilt-android-gradle-plugin:${deps.dagger.version}")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:${versions.spotless}")
    classpath("com.github.ben-manes:gradle-versions-plugin:${versions.gradleVersions}")
    classpath("com.squareup:javapoet:1.13.0")
    classpath("dev.icerock.moko:kswift-gradle-plugin:${versions.mokoKSwift}")
  }
}

plugins {
  googleKsp version versions.googleKsp apply false
  buildKonfig version versions.buildKonfig apply false
}

allprojects {
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      languageVersion = "1.8"
    }
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }

  // TODO: Workaround for https://github.com/google/dagger/issues/3448, https://github.com/google/dagger/issues/3459
  configurations.all {
    resolutionStrategy.eachDependency {
      if (requested.module.group == "org.jetbrains.kotlin" &&
        requested.module.name.startsWith("kotlin-stdlib")
      ) {
        useVersion(versions.kotlin)
      }
    }
  }
}

allprojects {
  apply<KoverPlugin>()
  configure<kotlinx.kover.api.KoverMergedConfig> {
    enable()
  }
  configure<kotlinx.kover.api.KoverProjectConfig> {
    filters { // common filters for all default Kover tasks
      classes { // common class filter for all default Kover tasks in this project
        excludes += excludedClasses
      }
    }
  }

  apply<SpotlessPlugin>()
  configure<SpotlessExtension> {
    val editorConfigKeys: Set<String> = hashSetOf(
      "ij_kotlin_imports_layout",
      "indent_size",
      "end_of_line",
      "charset",
      "continuation_indent_size"
    )

    kotlin {
      target("**/*.kt")

      // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
      val data = M[
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8",
        "continuation_indent_size" to "4",
        "disabled_rules" to L[
          "experimental:package-name",
          "experimental:trailing-comma",
          "experimental:type-parameter-list-spacing",
          "filename",
          "annotation"
        ].joinToString(separator = ",")
      ]

      ktlint(versions.ktlint)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in editorConfigKeys })
        .editorConfigOverride(data.filterKeys { it in editorConfigKeys })

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")

      val data = M[
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8",
        "continuation_indent_size" to "4"
      ]
      ktlint(versions.ktlint)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in editorConfigKeys })
        .editorConfigOverride(data.filterKeys { it in editorConfigKeys })

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }
}

subprojects {
  apply<KoverPlugin>()
  apply<VersionsPlugin>()

  fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
  }

  fun isStable(version: String) = !isNonStable(version)

  tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
      if (isStable(currentVersion)) {
        isNonStable(candidate.version)
      } else {
        false
      }
    }
  }

  afterEvaluate {
    tasks.withType<Test> {
      testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events = EnumSet.of(
          TestLogEvent.PASSED,
          TestLogEvent.FAILED,
          TestLogEvent.SKIPPED,
          TestLogEvent.STANDARD_OUT,
          TestLogEvent.STANDARD_ERROR
        )
        exceptionFormat = TestExceptionFormat.FULL
      }
    }
  }
}
