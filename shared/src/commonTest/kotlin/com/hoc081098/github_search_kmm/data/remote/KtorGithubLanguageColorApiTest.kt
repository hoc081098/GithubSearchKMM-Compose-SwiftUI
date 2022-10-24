package com.hoc081098.github_search_kmm.data.remote

import arrow.core.getOrHandle
import com.hoc081098.github_search_kmm.TestAntilog
import com.hoc081098.github_search_kmm.TestAppCoroutineDispatchers
import com.hoc081098.github_search_kmm.domain.model.ArgbColor
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest

class KtorGithubLanguageColorApiTest {
  private lateinit var ktorGithubLanguageColorApi: KtorGithubLanguageColorApi
  private lateinit var httpClient: HttpClient
  private lateinit var handlerChannel: Channel<MockRequestHandler>

  private val url = Url("https://127.0.0.1:8080")
  private val testAppCoroutineDispatchers = TestAppCoroutineDispatchers()
  private val json = createJson()
  private val antilog = TestAntilog()

  @BeforeTest
  fun setup() {
    Napier.base(antilog)

    handlerChannel = Channel(Channel.UNLIMITED)
    httpClient = createHttpClient(MockEngine, json) {
      addHandler { request ->
        handlerChannel.receive()(request)
      }
    }
    ktorGithubLanguageColorApi = KtorGithubLanguageColorApi(
      httpClient = httpClient,
      url = url,
      appCoroutineDispatchers = testAppCoroutineDispatchers
    )
  }

  @AfterTest
  fun teardown() {
    httpClient.close()
    handlerChannel.close()
    Napier.takeLogarithm(antilog)
  }

  @Test
  fun `getColors returns a Right WHEN httpClient returns a successful response and it is valid`() =
    runTest(testAppCoroutineDispatchers.testCoroutineDispatcher) {
      val colorsResponse = """
        |{
        |  "ABAP CDS": {
        |    "color": "#555e25",
        |    "url": "https://github.com/trending?l=ABAP-CDS"
        |  },
        |  "Ada": {
        |    "url": "https://github.com/trending?l=Ada"
        |  },
        |  "Agda": {
        |    "color": "#315665",
        |    "url": "https://github.com/trending?l=Agda"
        |  }
        |}
      """.trimMargin("|")

      handlerChannel.trySend { request ->
        when (request.url.encodedPath) {
          "" -> {
            check(request.method == HttpMethod.Get)

            respond(
              content = colorsResponse,
              status = HttpStatusCode.OK,
              headers = headersOf(
                HttpHeaders.ContentType,
                ContentType.Application.Json.toString()
              )
            )
          }
          else -> error("Unhandled request ${request.url}")
        }
      }

      val either = ktorGithubLanguageColorApi.getColors()
      val colors = either.getOrHandle { throw it }

      assertEquals(
        mapOf(
          "ABAP CDS" to ArgbColor.parse("#555e25").getOrHandle { fail(it) },
          "Agda" to ArgbColor.parse("#315665").getOrHandle { fail(it) },
        ),
        colors,
      )
    }
}
