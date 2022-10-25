package com.hoc081098.github_search_kmm.domain.model

import arrow.core.left
import com.hoc081098.github_search_kmm.getOrThrow
import com.hoc081098.github_search_kmm.readTextResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ArgbColorTest {
  @Test
  fun `ArgbColor_parse with a valid hex`() {
    assertArgbColor(hex = "#000000", a = 1f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(hex = "#FF000000", a = 1f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(hex = "#00000000", a = 0f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(
      hex = "#12345678",
      a = "12".in0to1,
      r = "34".in0to1,
      g = "56".in0to1,
      b = "78".in0to1
    )
    assertArgbColor(
      hex = "#123",
      a = 1f,
      r = "11".in0to1,
      g = "22".in0to1,
      b = "33".in0to1
    )

    // without # prefix
    assertArgbColor(hex = "000000", a = 1f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(hex = "FF000000", a = 1f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(hex = "00000000", a = 0f, r = 0f, g = 0f, b = 0f)
    assertArgbColor(
      hex = "12345678",
      a = "12".in0to1,
      r = "34".in0to1,
      g = "56".in0to1,
      b = "78".in0to1
    )
    assertArgbColor(
      hex = "123",
      a = 1f,
      r = "11".in0to1,
      g = "22".in0to1,
      b = "33".in0to1
    )
  }

  @Test
  fun `ArgbColor_parse with a valid hex _ from colors_json`() {
    Json.decodeFromString<List<String>>(readTextResource("colors.json")).forEach {
      assertTrue { ArgbColor.parse(it).isRight() }
    }
  }

  @Test
  fun `ArgbColor ==`() {
    assertEquals(
      ArgbColor.parse("#000000").getOrThrow,
      ArgbColor.parse("#000000").getOrThrow
    )
    assertEquals(
      ArgbColor.parse("#112233").getOrThrow,
      ArgbColor.parse("#123").getOrThrow
    )
    assertEquals(
      ArgbColor.parse("#FF112233").getOrThrow,
      ArgbColor.parse("#123").getOrThrow
    )
    assertEquals(
      ArgbColor.parse("#ff112233").getOrThrow,
      ArgbColor.parse("#123").getOrThrow
    )
    assertNotEquals(
      ArgbColor.parse("#FF112233").getOrThrow,
      ArgbColor.parse("#12112233").getOrThrow
    )
  }

  @Test
  fun `ArgbColor hashCode`() {
    assertEquals(
      ArgbColor.parse("#000000").getOrThrow.hashCode(),
      ArgbColor.parse("#000000").getOrThrow.hashCode()
    )
    assertEquals(
      ArgbColor.parse("#112233").getOrThrow.hashCode(),
      ArgbColor.parse("#123").getOrThrow.hashCode()
    )
    assertEquals(
      ArgbColor.parse("#FF112233").getOrThrow.hashCode(),
      ArgbColor.parse("#123").getOrThrow.hashCode()
    )
    assertEquals(
      ArgbColor.parse("#ff112233").getOrThrow.hashCode(),
      ArgbColor.parse("#123").getOrThrow.hashCode()
    )
    assertNotEquals(
      ArgbColor.parse("#FF112233").getOrThrow.hashCode(),
      ArgbColor.parse("#12112233").getOrThrow.hashCode()
    )
  }

  @Test
  fun `ArgbColor_parse with an invalid hex`() {
    assertInvalidHex(hex = "#")
    assertInvalidHex(hex = "#123456789")
    assertInvalidHex(hex = "#1234567")
    assertInvalidHex(hex = "#12345")
    assertInvalidHex(hex = "#1234")
    assertInvalidHex(hex = "#12")
    assertInvalidHex(hex = "#1")
    assertInvalidHex(hex = "")
    assertInvalidHex(hex = "@")
    assertInvalidHex(hex = "ABCDEFGH")
    assertInvalidHex(hex = "#12345XYZ")
  }

  private val String.in0to1 get() = toInt(radix = 16) / 255f

  private fun assertInvalidHex(hex: String) = assertEquals(
    expected = "Cannot convert $hex to Color".left(),
    actual = ArgbColor.parse(hex),
  )

  private fun assertArgbColor(hex: String, a: Float, r: Float, g: Float, b: Float) {
    ArgbColor.parse(hex)
      .getOrThrow
      .argb
      .run {
        assertEquals(
          expected = r,
          actual = red,
          absoluteTolerance = ABSOLUTE_TOLERANCE,
          message = "red"
        )
        assertEquals(
          expected = g,
          actual = green,
          absoluteTolerance = ABSOLUTE_TOLERANCE,
          message = "green"
        )
        assertEquals(
          expected = b,
          actual = blue,
          absoluteTolerance = ABSOLUTE_TOLERANCE,
          message = "blue"
        )
        assertEquals(
          expected = a,
          actual = alpha,
          absoluteTolerance = ABSOLUTE_TOLERANCE,
          message = "alpha"
        )
      }
  }

  private companion object {
    const val ABSOLUTE_TOLERANCE = 1e-3f
  }
}
