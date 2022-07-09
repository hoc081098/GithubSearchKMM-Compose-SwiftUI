package com.hoc081988.github_search_kmm

import com.hoc081988.github_search_kmm.data.dataModule
import com.hoc081988.github_search_kmm.domain.domainModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
  Napier.base(DebugAntilog())

  startKoin {
    appDeclaration()
    modules(
      dataModule,
      domainModule,
      appModule,
    )
  }
}
