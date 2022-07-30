package com.hoc081098.github_search_kmm.domain

import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
  factoryOf(::SearchRepoItemsUseCase)
}
