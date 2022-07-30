package com.hoc081098.github_search_kmm.data.remote

import arrow.core.Either
import com.hoc081098.github_search_kmm.domain.model.ArgbColor

interface GithubLanguageColorApi {
  suspend fun getColors(): Either<Throwable, Map<String, ArgbColor>>
}
