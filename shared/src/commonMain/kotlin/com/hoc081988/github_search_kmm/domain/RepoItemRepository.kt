package com.hoc081988.github_search_kmm.domain

interface RepoItemRepository {
  suspend fun searchRepos(
    term: String,
    page: Int,
  )
}
