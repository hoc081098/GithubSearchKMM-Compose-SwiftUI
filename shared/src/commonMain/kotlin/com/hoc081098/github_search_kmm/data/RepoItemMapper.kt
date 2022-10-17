package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem

internal fun RepoItemsSearchResponse.toRepoItemsList(colors: Map<String, ArgbColor>): List<RepoItem> =
  items
    ?.map { it.toRepoItem(colors) }
    ?: emptyList()

private fun RepoItemsSearchResponse.Item.toRepoItem(colors: Map<String, ArgbColor>): RepoItem =
  RepoItem(
    id = id,
    fullName = fullName,
    language = language,
    starCount = stargazersCount,
    name = name,
    repoDescription = description,
    languageColor = language?.let { colors[it] },
    htmlUrl = htmlUrl,
    owner = owner.toOwner(),
    updatedAt = updatedAt,
  )

private fun RepoItemsSearchResponse.Item.Owner.toOwner(): Owner = Owner(
  id = id,
  username = login,
  avatar = avatarUrl
)
