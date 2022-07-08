import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import java.util.EnumSet
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
    classpath("com.android.tools.build:gradle:${versions.agp}")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:${versions.spotless}")
    classpath("com.github.ben-manes:gradle-versions-plugin:${versions.gradleVersions}")
  }
}

allprojects {
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
    }
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

subprojects {
  apply<SpotlessPlugin>()
  apply<VersionsPlugin>()

  configure<SpotlessExtension> {
    val editorConfigKeys: Set<String> = hashSetOf(
      "ij_kotlin_imports_layout",
      "indent_size",
      "end_of_line",
      "charset",
      "continuation_indent_size",
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
          "filename"
        ].joinToString(separator = ","),
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
        "continuation_indent_size" to "4",
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

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
