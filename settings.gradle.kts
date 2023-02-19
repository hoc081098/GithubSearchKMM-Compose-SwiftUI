arrayOf("gradle.properties", "gradle").forEach(::copyToBuildSrc)

pluginManagement {
  repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

rootProject.name = "GithubSearchKMM"
include(":androidApp")
include(":shared")
include(":flowredux")

fun includeProject(name: String, filePath: String) {
  include(name)
  project(name).projectDir = File(filePath)
}

fun copyToBuildSrc(sourcePath: String) {
  rootDir.resolve(sourcePath).copyRecursively(
    target = rootDir.resolve("buildSrc").resolve(sourcePath),
    overwrite = true
  )
  rootDir.resolve(sourcePath).copyRecursively(
    target = rootDir.resolve("buildSrc")
      .resolve("buildSrc")
      .resolve(sourcePath),
    overwrite = true
  )
  println("[DONE] copied $sourcePath")
}
