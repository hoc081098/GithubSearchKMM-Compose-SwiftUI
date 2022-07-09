package com.hoc081988.github_search_kmm.com.hoc081988.github_search_kmm.data

import com.hoc081988.github_search_kmm.data.AppErrorMapper
import com.hoc081988.github_search_kmm.data.RepoItemRepositoryImpl
import com.hoc081988.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081988.github_search_kmm.data.remote.RepoItemApi
import javax.inject.Inject

internal class DaggerRepoItemRepositoryImpl @Inject constructor(
  repoItemApi: RepoItemApi,
  githubLanguageColorApi: GithubLanguageColorApi,
  errorMapper: AppErrorMapper
) : RepoItemRepositoryImpl(repoItemApi, githubLanguageColorApi, errorMapper)
