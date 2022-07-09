package com.hoc081988.github_search_kmm.data.remote

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

fun <T : HttpClientEngineConfig> createHttpClient(
  engineFactory: HttpClientEngineFactory<T>,
  block: T.() -> Unit,
): HttpClient = HttpClient(engineFactory) {
  engine(block)

  install(ContentNegotiation) {
    json(
      Json {
        serializersModule = SerializersModule {
          contextual(Instant::class, InstantSerializer)
        }
        ignoreUnknownKeys = true
        coerceInputValues = true
        prettyPrint = true
        isLenient = true
      }
    )
  }

  install(Logging) {
    level = LogLevel.ALL
    logger = object : Logger {
      override fun log(message: String) {
        Napier.d(message = message, tag = "[HttpClient]")
      }
    }
  }
}

internal object InstantSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
    "InstantSerializer",
    PrimitiveKind.STRING
  )

  override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
  override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

