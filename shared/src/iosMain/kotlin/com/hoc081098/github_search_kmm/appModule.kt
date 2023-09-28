package com.hoc081098.github_search_kmm

import kotlin.experimental.ExperimentalNativeApi
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val IoAppCoroutineScope = named("IoAppCoroutineScope")
val MainAppCoroutineScope = named("MainAppCoroutineScope")

@OptIn(ExperimentalNativeApi::class)
actual fun isDebug(): Boolean = Platform.isDebugBinary

val AppModule = module {
  singleOf(::IosAppCoroutineDispatchers) {
    bind<AppCoroutineDispatchers>()
  }

  single<AppCoroutineScope>(IoAppCoroutineScope) {
    new(::IoAppCoroutineScopeImpl)
  }

  single<AppCoroutineScope>(MainAppCoroutineScope) {
    new(::MainAppCoroutineScopeImpl)
  }
}
