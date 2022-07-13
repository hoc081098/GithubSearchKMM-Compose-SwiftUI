package com.hoc081988.github_search_kmm.data

import com.hoc081988.github_search_kmm.data.remote.CacheGithubLanguageColorApiDecorator
import com.hoc081988.github_search_kmm.data.remote.DaggerKtorGithubLanguageColorApi
import javax.inject.Inject

internal class DaggerCacheGithubLanguageColorApiDecorator @Inject constructor(
  decoratee: DaggerKtorGithubLanguageColorApi
) : CacheGithubLanguageColorApiDecorator(decoratee)