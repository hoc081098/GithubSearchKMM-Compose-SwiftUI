package com.hoc081098.github_search_kmm.data.remote

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
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

fun createJson(): Json = Json {
  serializersModule = SerializersModule {
    contextual(Instant::class, InstantSerializer)
  }
  ignoreUnknownKeys = true
  coerceInputValues = true
  prettyPrint = true
  isLenient = true
  encodeDefaults = true
  allowSpecialFloatingPointValues = true
  allowStructuredMapKeys = true
  useArrayPolymorphism = false
}

fun <T : HttpClientEngineConfig> createHttpClient(
  engineFactory: HttpClientEngineFactory<T>,
  json: Json,
  block: T.() -> Unit,
): HttpClient = HttpClient(engineFactory) {
  engine(block)

  install(HttpTimeout) {
    requestTimeoutMillis = 15_000
    connectTimeoutMillis = 10_000
    socketTimeoutMillis = 10_000
  }

  install(ContentNegotiation) {
    json(json)
    register(
      ContentType.Text.Plain,
      KotlinxSerializationConverter(json)
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
