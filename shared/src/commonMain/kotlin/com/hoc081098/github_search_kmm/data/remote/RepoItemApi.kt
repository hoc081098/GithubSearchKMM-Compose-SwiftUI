package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse

internal interface RepoItemApi {
  suspend fun searchRepoItems(
    term: String,
    page: Int
  ): Either<Throwable, RepoItemsSearchResponse>
}
