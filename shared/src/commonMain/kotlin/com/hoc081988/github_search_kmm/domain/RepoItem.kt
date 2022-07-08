package com.hoc081988.github_search_kmm.domain

import kotlin.jvm.JvmInline

data class RepoItem(
  val fullName: String,
  val language: String?,
  val starCount: Int,
  val name: String,
  val description: String?,
  val languageColor: Color?,
  val htmlUrl: String,
  val owner: Owner,
)

@JvmInline
value class Color(val value: String)

data class Owner(
  val username: String,
  val avatar: String
)
