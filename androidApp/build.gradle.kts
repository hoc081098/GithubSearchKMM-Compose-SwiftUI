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

  implementation(deps.androidx.appCompat)
  implementation(deps.androidx.coreKtx)

  implementation(deps.lifecycle.viewModelKtx)
  implementation(deps.lifecycle.runtimeKtx)

  implementation(deps.androidx.recyclerView)
  implementation(deps.androidx.constraintLayout)
  implementation(deps.androidx.swipeRefreshLayout)
  implementation(deps.androidx.material)

  implementation(deps.coroutines.core)
  implementation(deps.coroutines.android)

  implementation(deps.coil)
  implementation(deps.napier)

  implementation(deps.viewBindingDelegate)
  implementation(deps.flowExt)

  implementation(deps.dagger.hiltAndroid)
  kapt(deps.dagger.hiltAndroidCompiler)
}
