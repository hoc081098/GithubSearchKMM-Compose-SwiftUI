package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.utils.parZipEither
import io.github.aakira.napier.Napier

internal open class RepoItemRepositoryImpl(
  private val repoItemApi: RepoItemApi,
  private val githubLanguageColorApi: GithubLanguageColorApi,
  private val errorMapper: AppErrorMapper,
  private val appCoroutineDispatchers: AppCoroutineDispatchers,
) : RepoItemRepository {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ) = parZipEither(
    ctx = appCoroutineDispatchers.io,
    fa = {
      githubLanguageColorApi
        .getColors()
        .mapLeft(errorMapper)
        .onLeft {
          Napier.e(
            message = "githubLanguageColorApi.getColors()",
            throwable = it,
            tag = "RepoItemRepositoryImpl"
          )
        }
    },
    fb = {
      repoItemApi
        .searchRepoItems(
          term = term,
          page = page
        )
        .mapLeft(errorMapper)
        .onLeft {
          Napier.e(
            message = "repoItemApi.searchRepoItems(term=$term, page=$page)",
            throwable = it,
            tag = "RepoItemRepositoryImpl"
          )
        }
    }
  ) { colors, repoItemsSearchResponse ->
    repoItemsSearchResponse.toRepoItemsList(colors)
  }
}
