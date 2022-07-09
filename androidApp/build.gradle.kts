plugins {
  androidApplication
  kotlinAndroid
  kotlinKapt
  kotlinParcelize
}

android {
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
}

dependencies {
  implementation(project(":shared"))

  implementation(deps.androidx.material)
  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.constraintLayout)
}
