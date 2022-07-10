

package com.hoc081988.github_search_kmm

import com.hoc081988.github_search_kmm.data.dataModule
import com.hoc081988.github_search_kmm.domain.domainModule
import com.hoc081988.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

object KoinHelper : KoinComponent {
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

  fun searchRepoItemsUseCase(): SearchRepoItemsUseCase = get()
}
