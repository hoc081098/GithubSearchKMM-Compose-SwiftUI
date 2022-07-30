package com.hoc081098.github_search_kmm.domain.repository

import arrow.core.Either
import com.hoc081098.github_search_kmm.domain.model.AppError
import com.hoc081098.github_search_kmm.domain.model.RepoItem

interface RepoItemRepository {
  suspend fun searchRepoItems(
    term: String,
    page: Int,
  ): Either<AppError, List<RepoItem>>
}
