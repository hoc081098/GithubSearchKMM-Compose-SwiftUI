package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.withContext

typealias ColorsResponseType = Map<String, Map<String, String?>>

internal open class KtorGithubLanguageColorApi(
  private val url: Url,
  private val httpClient: HttpClient,
  private val appCoroutineDispatchers: AppCoroutineDispatchers,
) : GithubLanguageColorApi {
  override suspend fun getColors() = withContext(appCoroutineDispatchers.io) {
    Either
      .catch {
        httpClient
          .get(url)
          .body<ColorsResponseType>()
      }
      .flatMap { it.toColors() }
  }

  private fun ColorsResponseType.toColors(): Either<IllegalStateException, Map<String, ArgbColor>> {
    return mapNotNull { (k, v) ->
      v["color"]?.let {
        k to it
      }
    }
      .let { pairs ->
        either {
          pairs.map { (k, v) ->
            ArgbColor
              .parse(v)
              .mapLeft(::IllegalStateException)
              .map { k to it }
              .bind()
          }
        }
      }
      .map { it.toMap() }
  }
}
