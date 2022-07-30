package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.path
import kotlinx.coroutines.withContext

internal open class KtorRepoItemApi(
  private val httpClient: HttpClient,
  private val baseUrl: Url,
  private val appCoroutineDispatchers: AppCoroutineDispatchers,
) : RepoItemApi {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ) = withContext(appCoroutineDispatchers.io) {
    Either.catch {
      httpClient.get(
        URLBuilder(baseUrl)
          .apply {
            path("search/repositories")
            parameters.append("q", term)
            parameters.append("page", page.toString())
          }
          .build()
      ).body<RepoItemsSearchResponse>()
    }
  }
}
