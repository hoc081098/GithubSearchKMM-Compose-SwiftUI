package com.hoc081988.github_search_kmm.data

import com.hoc081988.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081988.github_search_kmm.data.remote.RepoItemApi
import com.hoc081988.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import com.hoc081988.github_search_kmm.domain.model.Color
import com.hoc081988.github_search_kmm.domain.model.Owner
import com.hoc081988.github_search_kmm.domain.model.RepoItem
import com.hoc081988.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081988.github_search_kmm.parZipEither
import io.github.aakira.napier.Napier

internal open class RepoItemRepositoryImpl(
  private val repoItemApi: RepoItemApi,
  private val githubLanguageColorApi: GithubLanguageColorApi,
  private val errorMapper: AppErrorMapper,
) : RepoItemRepository {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ) = parZipEither(
    fa = {
      githubLanguageColorApi
        .getColors()
        .mapLeft(errorMapper)
        .tapLeft {
          Napier.e(
            message = "githubLanguageColorApi.getColors()",
            throwable = it,
            tag = "RepoItemRepositoryImpl"
          )
        }
    },
    fb = {
      repoItemApi
        .searchRepoItems(
          term = term,
          page = page
        )
        .mapLeft(errorMapper)
        .tapLeft {
          Napier.e(
            message = "repoItemApi.searchRepoItems(term=$term, page=$page)",
            throwable = it,
            tag = "RepoItemRepositoryImpl"
          )
        }
    }
  ) { colors, repoItemsSearchResponse ->
    repoItemsSearchResponse.toRepoItemsList(colors)
  }
}

private fun RepoItemsSearchResponse.toRepoItemsList(colors: Map<String, Color>): List<RepoItem> =
  items?.map { item ->
    RepoItem(
      id = item.id,
      fullName = item.fullName,
      language = item.language,
      starCount = item.stargazersCount,
      name = item.name,
      description = item.description,
      languageColor = item.language?.let { colors[it] },
      htmlUrl = item.htmlUrl,
      owner = item.owner.toOwner(),
      updatedAt = item.updatedAt,
    )
  } ?: emptyList()

private fun RepoItemsSearchResponse.Item.Owner.toOwner(): Owner = Owner(
  id = id,
  username = login,
  avatar = avatarUrl
)
