package com.hoc081098.github_search_kmm.data.remote.response

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepoItemsSearchResponse(
  @SerialName("total_count") val totalCount: Int, // 4692
  @SerialName("incomplete_results") val incompleteResults: Boolean, // false
  @SerialName("items") val items: List<Item>? = null
) {
  @Serializable
  data class Item(
    @SerialName("id") val id: Int, // 45781894
    @SerialName("name") val name: String, // MPParallaxView
    @SerialName("full_name") val fullName: String, // DroidsOnRoids/MPParallaxView
    @SerialName("owner") val owner: Owner,
    @SerialName("html_url") val htmlUrl: String, // https://github.com/DroidsOnRoids/MPParallaxView
    @SerialName("description") val description: String?, // Apple TV Parallax effect in Swift.
    @SerialName("updated_at") val updatedAt: Instant, // 2022-06-30T12:02:54Z
    @SerialName("stargazers_count") val stargazersCount: Int, // 1737
    @SerialName("language") val language: String?, // Swift
  ) {
    @Serializable
    data class Owner(
      @SerialName("login") val login: String, // DroidsOnRoids
      @SerialName("id") val id: Int, // 2815187
      @SerialName("avatar_url") val avatarUrl: String, // https://avatars.githubusercontent.com/u/2815187?v=4
    )
  }
}
