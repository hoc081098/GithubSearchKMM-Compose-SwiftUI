package com.hoc081098.github_search_kmm.data

import com.hoc081098.github_search_kmm.AppCoroutineDispatchers
import com.hoc081098.github_search_kmm.data.remote.GithubLanguageColorApi
import com.hoc081098.github_search_kmm.data.remote.RepoItemApi
import com.hoc081098.github_search_kmm.data.remote.response.RepoItemsSearchResponse
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import com.hoc081098.github_search_kmm.domain.model.Owner
import com.hoc081098.github_search_kmm.domain.model.RepoItem
import com.hoc081098.github_search_kmm.domain.repository.RepoItemRepository
import com.hoc081098.github_search_kmm.utils.parZipEither
import io.github.aakira.napier.Napier

internal open class RepoItemRepositoryImpl(
  private val repoItemApi: RepoItemApi,
  private val githubLanguageColorApi: GithubLanguageColorApi,
  private val errorMapper: AppErrorMapper,
  private val appCoroutineDispatchers: AppCoroutineDispatchers,
) : RepoItemRepository {
  override suspend fun searchRepoItems(
    term: String,
    page: Int
  ) = parZipEither(
    ctx = appCoroutineDispatchers.io,
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
