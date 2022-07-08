package com.hoc081988.github_search_kmm.data

import arrow.core.Either
import com.hoc081988.github_search_kmm.data.remote.RepoItemApi
import com.hoc081988.github_search_kmm.domain.model.AppError
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import com.hoc081988.github_search_kmm.domain.repository.RepoItemRepository

internal open class RepoItemRepositoryImpl(
  private val repoItemApi: RepoItemApi,
) : RepoItemRepository {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ): Either<AppError, List<RepoItem>> {
    return repoItemApi
      .searchRepoItems(
        term = term,
        page = page
      )
      .map { TODO() }
      .mapLeft {
        TODO()
      }
  }
}
