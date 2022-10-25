package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.getOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.Instant

class RepoItemMapperTest {
  @Test
  fun `test RepoItemsSearchResponse_toRepoItemsList`() {
    val response = RepoItemsSearchResponse(
      totalCount = 10,
      incompleteResults = false,
      items = listOf(
        RepoItemsSearchResponse.Item(
          id = 1,
          name = "name 1",
          fullName = "fullName 1",
          owner = RepoItemsSearchResponse.Item.Owner(
            login = "owner1",
            id = 0,
            avatarUrl = "owner1/avatar"
          ),
          htmlUrl = "url/1",
          description = "description 1",
          updatedAt = Instant.fromEpochMilliseconds(1),
          stargazersCount = 10,
          language = "Kotlin"
        ),
        RepoItemsSearchResponse.Item(
          id = 2,
          name = "name 2",
          fullName = "fullName 2",
          owner = RepoItemsSearchResponse.Item.Owner(
            login = "owner2",
            id = 2,
            avatarUrl = "owner2/avatar"
          ),
          htmlUrl = "url/2",
          description = "description 2",
          updatedAt = Instant.fromEpochMilliseconds(2),
          stargazersCount = 20,
          language = "Scala"
        )
      )
    )

    val colors = mapOf(
      "Kotlin" to ArgbColor.parse("#F18E33").getOrThrow,
    )

    val items = response.toRepoItemsList(colors)

    assertEquals(
      expected = listOf(
        RepoItem(
          id = 1,
          name = "name 1",
          fullName = "fullName 1",
          owner = Owner(
            id = 0,
            username = "owner1",
            avatar = "owner1/avatar"
          ),
          htmlUrl = "url/1",
          repoDescription = "description 1",
          updatedAt = Instant.fromEpochMilliseconds(1),
          starCount = 10,
          language = "Kotlin",
          languageColor = colors["Kotlin"]!!
        ),
        RepoItem(
          id = 2,
          name = "name 2",
          fullName = "fullName 2",
          owner = Owner(
            id = 2,
            username = "owner2",
            avatar = "owner2/avatar"
          ),
          htmlUrl = "url/2",
          repoDescription = "description 2",
          updatedAt = Instant.fromEpochMilliseconds(2),
          starCount = 20,
          language = "Scala",
          languageColor = null
        )
      ),
      actual = items
    )
  }
}
