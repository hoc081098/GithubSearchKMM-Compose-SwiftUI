package com.hoc081098.github_search_kmm.domain

import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.domain.usecase.SearchRepoItemsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DomainModule {
  companion object {
    @Provides
    internal fun searchRepoItemsUseCase(
      repoItemRepository: RepoItemRepository,
    ): SearchRepoItemsUseCase = SearchRepoItemsUseCase(repoItemRepository)
  }
}
