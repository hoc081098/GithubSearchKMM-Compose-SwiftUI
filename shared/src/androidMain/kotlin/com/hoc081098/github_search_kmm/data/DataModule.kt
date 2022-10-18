package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.data.remote.DaggerKtorRepoItemApi
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import com.hoc081098.github_search_kmm.data.remote.createHttpClient
import com.hoc081098.github_search_kmm.data.remote.createJson
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.http.Url
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
internal annotation class GithubLanguageColorApiUrl

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
internal annotation class RepoItemApiBaseUrl

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {
  @Binds
  @Singleton
  fun repoItemRepository(impl: DaggerRepoItemRepositoryImpl): RepoItemRepository

  @Binds
  @Singleton
  fun repoItemApi(impl: DaggerKtorRepoItemApi): RepoItemApi

  @Binds
  @Singleton
  fun githubLanguageColorApi(impl: DaggerCacheGithubLanguageColorApiDecorator): GithubLanguageColorApi

  @Binds
  @Singleton
  fun appErrorMapper(impl: DaggerAppErrorMapperImpl): AppErrorMapper

  companion object {
    @Provides
    @GithubLanguageColorApiUrl
    internal fun githubLanguageColorApiUrl(): Url =
      Url("https://github.com/ozh/github-colors/raw/master/colors.json")

    @Provides
    @RepoItemApiBaseUrl
    internal fun repoItemApiBaseUrl(): Url = Url("https://api.github.com")

    @Provides
    @Singleton
    internal fun json(): Json = createJson()

    @Provides
    @Singleton
    internal fun httpClient(json: Json): HttpClient = createHttpClient(
      engineFactory = OkHttp,
      json = json
    ) {}
  }
}
