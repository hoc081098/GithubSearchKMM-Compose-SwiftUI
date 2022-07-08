package com.hoc081988.github_search_kmm.data.remote

import arrow.core.Either
import com.hoc081988.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

internal open class KtorRepoItemApi(
  private val httpClient: HttpClient,
  private val baseUrl: String,
) : RepoItemApi {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ) = Either.catch {
    httpClient.get(
      URLBuilder(baseUrl)
        .apply {
          path("search/repositories")
          parameters.append("q", term)
          parameters.append("page", page.toString())
        }
        .toString()
    ).body<RepoItemsSearchResponse>()
  }
}
