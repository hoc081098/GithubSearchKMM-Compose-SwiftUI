package com.hoc081988.github_search_kmm.data.remote

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.traverse
import com.hoc081988.github_search_kmm.domain.model.Color
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url

internal open class KtorGithubLanguageColorApi(
  private val url: Url,
  private val httpClient: HttpClient,
) : GithubLanguageColorApi {
  override suspend fun getColors() = Either
    .catch {
      httpClient
        .get(url)
        .body<Map<String, String>>()
    }
    .flatMap { map ->
      map
        .toList()
        .traverse { (k, v) ->
          Color
            .parse(v)
            .mapLeft { IllegalStateException(it) }
            .map { k to it }
        }
    }
    .map { it.toMap() }
}
