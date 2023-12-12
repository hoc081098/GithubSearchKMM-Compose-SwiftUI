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
    }
    compilerOptions {
      languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9
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
    filters {
      // common filters for all default Kover tasks
      classes {
        // common class filter for all default Kover tasks in this project
        excludes += excludedClasses
      }
    }
  }

  apply<SpotlessPlugin>()
  configure<SpotlessExtension> {
    kotlin {
      target("**/*.kt")
      targetExclude("**/build/**/*.kt", "**/.gradle/**/*.kt")

      ktlint(versions.ktlint)
        .setEditorConfigPath(rootProject.file(".editorconfig"))

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")
      targetExclude("**/build/**/*.xml", "**/.idea/**/*.xml", "**/.gradle/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
      lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")
      targetExclude("**/build/**/*.kts", "**/.gradle/**/*.kts")

      ktlint(versions.ktlint)
        .setEditorConfigPath(rootProject.file(".editorconfig"))

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
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
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
          TestLogEvent.STANDARD_ERROR,
        )
        exceptionFormat = TestExceptionFormat.FULL
      }
    }
  }
}
