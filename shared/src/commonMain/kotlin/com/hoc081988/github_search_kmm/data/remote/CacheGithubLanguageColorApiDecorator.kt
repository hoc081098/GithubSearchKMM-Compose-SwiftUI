package com.hoc081988.github_search_kmm.data.remote

import arrow.core.right
import com.hoc081988.github_search_kmm.domain.model.Color
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal open class CacheGithubLanguageColorApiDecorator(
  private val decoratee: GithubLanguageColorApi,
) : GithubLanguageColorApi {
  private val mutex = Mutex()
  private val cache = atomic<Map<String, Color>?>(null)

  override suspend fun getColors() = mutex.withLock {
    cache.value?.let {
      Napier.d(message = "Hit cache...", tag = "CacheGithubLanguageColorApiDecorator")
      return it.right()
    }

    Napier.d(message = "Call $decoratee.getColors", tag = "CacheGithubLanguageColorApiDecorator")
    decoratee
      .getColors()
      .tap { cache.value = it }
  }
}
