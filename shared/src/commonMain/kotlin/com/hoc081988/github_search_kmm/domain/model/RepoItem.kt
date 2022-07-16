package com.hoc081988.github_search_kmm.domain.model

import arrow.core.Either
import kotlinx.datetime.Instant

data class RepoItem(
  val id: Int,
  val fullName: String,
  val language: String?,
  val starCount: Int,
  val name: String,
  val repoDescription: String?,
  val languageColor: Color?,
  val htmlUrl: String,
  val owner: Owner,
  val updatedAt: Instant,
)

class Color constructor(
  val value: ULong,
  val hexStringWithoutPrefix: String,
) {
  companion object {
    /**
     * Retrieves a [Color] from the provided [hex]. The provided [hex] must be in a
     * valid Hex Color format, with or without the preceeding '#' character.
     *
     * @return a [arrow.core.Either.Left] if the provided [hex] is not in a valid Hex Color format.
     */
    fun parse(hex: String): Either<String, Color> {
      return Either.catch {
        var formattedHexString = hex.trim()
          .removePrefix("#")
          .lowercase()

        if (formattedHexString.length == 3) {
          // Shorthand hex value was used so expand it
          val chars = formattedHexString.toCharArray()
          formattedHexString = "${chars[0]}${chars[0]}${chars[1]}${chars[1]}${chars[2]}${chars[2]}"
        }

        var colorInt = formattedHexString.toLong(radix = 16).toInt()
        if (formattedHexString.length == 6) {
          // Add the alpha channel
          colorInt = colorInt or -0x1000000
        }

        Color(
          value = (colorInt.toULong() and 0xffffffffUL) shl 32,
          hexStringWithoutPrefix = formattedHexString
        )
      }.mapLeft { "Cannot convert $hex to Color" }
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    return value == (other as Color).value
  }

  override fun hashCode() = value.hashCode()

  override fun toString() =
    "Color(value=$value, hexStringWithoutPrefix='$hexStringWithoutPrefix')"
}

data class Owner(
  val id: Int,
  val username: String,
  val avatar: String
)
