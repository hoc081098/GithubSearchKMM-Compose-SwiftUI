package com.hoc081988.github_search_kmm.data.remote

import com.hoc081988.github_search_kmm.data.GithubLanguageColorApiUrl
import io.ktor.client.HttpClient
import io.ktor.http.Url
import javax.inject.Inject

internal class DaggerKtorGithubLanguageColorApi @Inject constructor(
  @GithubLanguageColorApiUrl url: Url,
  httpClient: HttpClient
) : KtorGithubLanguageColorApi(url, httpClient)
