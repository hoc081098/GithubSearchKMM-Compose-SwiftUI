package com.hoc081098.github_search_kmm.presentation

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val presentationModule = module {
  factoryOf(::GithubSearchViewModel)
}
