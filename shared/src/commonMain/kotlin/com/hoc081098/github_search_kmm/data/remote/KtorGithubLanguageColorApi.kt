package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.traverse
import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.withContext

typealias ColorResponseType = Map<String, Map<String, String?>>

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
          .body<ColorResponseType>()
      }
      .flatMap { it.toColors() }
  }

  private fun ColorResponseType.toColors(): Either<IllegalStateException, Map<String, ArgbColor>> {
    return mapNotNull { (k, v) ->
      v["color"]?.let {
        k to it
      }
    }
      .traverse { (k, v) ->
        ArgbColor
          .parse(v)
          .mapLeft(::IllegalStateException)
          .map { k to it }
      }
      .map { it.toMap() }
  }
}
