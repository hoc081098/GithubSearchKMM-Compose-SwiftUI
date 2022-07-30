package com.hoc081098.github_search_kmm.data.remote

import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.GithubLanguageColorApiUrl
import io.ktor.client.HttpClient
import io.ktor.http.Url
import javax.inject.Inject

internal class DaggerKtorGithubLanguageColorApi @Inject constructor(
  @GithubLanguageColorApiUrl url: Url,
  httpClient: HttpClient,
  appCoroutineDispatchers: AppCoroutineDispatchers
) : KtorGithubLanguageColorApi(
  url = url,
  httpClient = httpClient,
  appCoroutineDispatchers = appCoroutineDispatchers
)
