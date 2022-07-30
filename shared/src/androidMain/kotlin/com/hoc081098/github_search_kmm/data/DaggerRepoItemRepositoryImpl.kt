package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import javax.inject.Inject

internal class DaggerRepoItemRepositoryImpl @Inject constructor(
  repoItemApi: RepoItemApi,
  githubLanguageColorApi: GithubLanguageColorApi,
  errorMapper: AppErrorMapper,
  appCoroutineDispatchers: AppCoroutineDispatchers
) : RepoItemRepositoryImpl(
  repoItemApi = repoItemApi,
  githubLanguageColorApi = githubLanguageColorApi,
  errorMapper = errorMapper,
  appCoroutineDispatchers = appCoroutineDispatchers
)
