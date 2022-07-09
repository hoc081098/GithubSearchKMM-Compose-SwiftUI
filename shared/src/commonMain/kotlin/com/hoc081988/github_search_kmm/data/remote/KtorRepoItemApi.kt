package com.hoc081988.github_search_kmm.data.remote

import arrow.core.Either
import com.hoc081988.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.path

internal open class KtorRepoItemApi(
  private val httpClient: HttpClient,
  private val baseUrl: Url,
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
