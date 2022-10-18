package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.data.remote.CacheGithubLanguageColorApiDecorator
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.KtorGithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.KtorRepoItemApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import com.hoc081098.github_search_kmm.data.remote.createHttpClient
import com.hoc081098.github_search_kmm.data.remote.createJson
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import io.ktor.client.engine.darwin.Darwin
import io.ktor.http.Url
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val GithubLanguageColorApiUrl = named("GithubLanguageColorApiUrl")
private val RepoItemApiBaseUrl = named("RepoItemApiBaseUrl")

val dataModule = module {
  singleOf(::RepoItemRepositoryImpl) {
    bind<RepoItemRepository>()
  }

  single<RepoItemApi> {
    KtorRepoItemApi(
      httpClient = get(),
      baseUrl = get(RepoItemApiBaseUrl),
      appCoroutineDispatchers = get()
    )
  }

  factory(RepoItemApiBaseUrl) {
    Url("https://api.github.com")
  }

  single {
    KtorGithubLanguageColorApi(
      url = get(GithubLanguageColorApiUrl),
      httpClient = get(),
      appCoroutineDispatchers = get()
    )
  }

  single<GithubLanguageColorApi> {
    CacheGithubLanguageColorApiDecorator(
      decoratee = get<KtorGithubLanguageColorApi>()
    )
  }

  factory(GithubLanguageColorApiUrl) {
    Url("https://github.com/ozh/github-colors/raw/master/colors.json")
  }

  singleOf(::AppErrorMapperImpl) {
    bind<AppErrorMapper>()
  }

  singleOf(::PlatformAppErrorMapper)

  singleOf(::createJson)

  single {
    createHttpClient(
      engineFactory = Darwin,
      json = get()
    ) {}
  }
}
