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
  val languageColor: ArgbColor?,
  val htmlUrl: String,
  val owner: Owner,
  val updatedAt: Instant,
)

class ArgbColor private constructor(
  val hexStringWithoutPrefix: String,
) {
  data class Argb(
    val red: Float,
    val green: Float,
    val blue: Float,
    val alpha: Float,
  )

  fun argb(): Argb? {
    val int = hexStringWithoutPrefix.toULong(radix = 16)

    val a: ULong
    val r: ULong
    val g: ULong
    val b: ULong

    when (hexStringWithoutPrefix.length) {
      3 -> {
        // RGB (12-bit)
        a = 255u
        r = (int shr 8) * 17u
        g = (int shr 4 and 0xFu) * 17u
        b = (int and 0xFu) * 17u
      }
      6 -> {
        // RGB (24-bit)
        a = 255u
        r = int shr 16
        g = int shr 8 and 0xFFu
        b = int and 0xFFu
      }
      8 -> {
        // ARGB (32-bit)
        a = int shr 24
        r = int shr 16 and 0xFFu
        g = int shr 8 and 0xFFu
        b = int and 0xFFu
      }
      else -> return null
    }

    return Argb(
      red = r.toFloat() / 255f,
      green = g.toFloat() / 255f,
      blue = b.toFloat() / 255f,
      alpha = a.toFloat() / 255f
    )
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || this::class != other::class) return false
    other as ArgbColor
    return hexStringWithoutPrefix == other.hexStringWithoutPrefix
  }

  override fun hashCode() = hexStringWithoutPrefix.hashCode()

  override fun toString() = "ArgbColor(hexStringWithoutPrefix='$hexStringWithoutPrefix')"

  companion object {
    /**
     * Retrieves a [ArgbColor] from the provided [hex]. The provided [hex] must be in a
     * valid Hex Color format, with or without the preceeding '#' character.
     *
     * @return a [arrow.core.Either.Left] if the provided [hex] is not in a valid Hex Color format.
     */
    fun parse(hex: String): Either<String, ArgbColor> {
      return Either.catch {
        var formattedHexString = hex.trim()
          .removePrefix("#")
          .lowercase()

        if (formattedHexString.length == 3) {
          // Shorthand hex value was used so expand it
          val chars = formattedHexString.toCharArray()
          formattedHexString = "${chars[0]}${chars[0]}${chars[1]}${chars[1]}${chars[2]}${chars[2]}"
        }

        ArgbColor(
          hexStringWithoutPrefix = formattedHexString
        )
      }.mapLeft { "Cannot convert $hex to Color" }
    }
  }
}

data class Owner(
  val id: Int,
  val username: String,
  val avatar: String
)
