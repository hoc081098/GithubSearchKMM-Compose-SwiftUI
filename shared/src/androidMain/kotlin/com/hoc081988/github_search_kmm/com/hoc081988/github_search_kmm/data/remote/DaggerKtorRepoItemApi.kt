package com.hoc081988.github_search_kmm.com.hoc081988.github_search_kmm.data.remote

import com.hoc081988.github_search_kmm.com.hoc081988.github_search_kmm.data.RepoItemApiBaseUrl
import com.hoc081988.github_search_kmm.data.remote.KtorRepoItemApi
import io.ktor.client.HttpClient
import io.ktor.http.Url
import javax.inject.Inject

internal class DaggerKtorRepoItemApi @Inject constructor(
  httpClient: HttpClient,
  @RepoItemApiBaseUrl baseUrl: Url
) : KtorRepoItemApi(httpClient, baseUrl)
