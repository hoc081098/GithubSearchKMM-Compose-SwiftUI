package com.hoc081988.github_search_kmm

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
  singleOf(::IosAppCoroutineDispatchers) {
    bind<AppCoroutineDispatchers>()
  }
}
