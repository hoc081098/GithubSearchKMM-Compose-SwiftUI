package com.hoc081098.github_search_kmm

import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import kotlin.random.Random
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.Clock

fun genRepoItems(ids: IntRange): PersistentList<RepoItem> = ids.map { id ->
  RepoItem(
    id = id,
    fullName = "Full name: $id",
    language = "Kotlin",
    starCount = Random.nextInt(),
    name = "Name: $id",
    repoDescription = "Description: $id",
    languageColor = ArgbColor.parse("#FF112233").getOrThrow,
    htmlUrl = "url/$id",
    owner = Owner(
      id = id,
      username = "username $id",
      avatar = "avatar/$id"
    ),
    updatedAt = Clock.System.now()
  )
}.toPersistentList()
