package com.hoc081098.github_search_kmm.domain.usecase

import arrow.core.Either
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository

class SearchRepoItemsUseCase(
  private val repoItemRepository: RepoItemRepository,
) {
  suspend operator fun invoke(
    term: String,
    page: Int
  ): Either<AppError, List<RepoItem>> = repoItemRepository.searchRepoItems(
    term = term,
    page = page
  )
}
