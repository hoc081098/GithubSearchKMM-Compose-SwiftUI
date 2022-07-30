package com.hoc081098.github_search_kmm.data.remote

import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.RepoItemApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.http.Url
import javax.inject.Inject

internal class DaggerKtorRepoItemApi @Inject constructor(
  httpClient: HttpClient,
  @RepoItemApiBaseUrl baseUrl: Url,
  appCoroutineDispatchers: AppCoroutineDispatchers
) : KtorRepoItemApi(
  httpClient = httpClient,
  baseUrl = baseUrl,
  appCoroutineDispatchers = appCoroutineDispatchers
)
