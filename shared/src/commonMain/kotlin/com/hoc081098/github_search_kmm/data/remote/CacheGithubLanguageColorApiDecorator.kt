package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import arrow.core.right
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal open class CacheGithubLanguageColorApiDecorator(
  private val decoratee: GithubLanguageColorApi,
) : GithubLanguageColorApi {
  private val mutex = Mutex()
  private val cache = atomic<Map<String, ArgbColor>?>(null)

  override suspend fun getColors(): Either<Throwable, Map<String, ArgbColor>> {
    cache.value?.let {
      Napier.d(message = "Hit cache 1...", tag = "CacheGithubLanguageColorApiDecorator")
      return it.right()
    }

    return mutex.withLock {
      cache.value?.let {
        Napier.d(message = "Hit cache 2...", tag = "CacheGithubLanguageColorApiDecorator")
        return it.right()
      }

      Napier.d(message = "Call $decoratee.getColors", tag = "CacheGithubLanguageColorApiDecorator")
      decoratee
        .getColors()
        .onRight { cache.value = it }
    }
  }
}
