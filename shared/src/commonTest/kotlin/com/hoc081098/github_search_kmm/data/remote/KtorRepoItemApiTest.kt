package com.hoc081098.github_search_kmm.data.remote

import com.hoc081098.github_search_kmm.TestAntilog
import com.hoc081098.github_search_kmm.TestAppCoroutineDispatchers
import com.hoc081098.github_search_kmm.getOrThrow
import com.hoc081098.github_search_kmm.leftValueOrThrow
import com.hoc081098.github_search_kmm.readTextResource
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString

class KtorRepoItemApiTest {
  private lateinit var ktorRepoItemApi: KtorRepoItemApi
  private lateinit var httpClient: HttpClient
  private lateinit var handlerChannel: Channel<MockRequestHandler>

  private val baseUrl = Url("https://127.0.0.1:8080")
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
    ktorRepoItemApi = KtorRepoItemApi(
      httpClient = httpClient,
      baseUrl = baseUrl,
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
  fun `searchRepoItems returns a Right WHEN httpClient returns a successful response`() =
    runTest(testAppCoroutineDispatchers.testCoroutineDispatcher) {
      val responseString = readTextResource("search_repositories_response.json")

      handlerChannel.trySend { request ->
        when (request.url.encodedPath) {
          "/search/repositories" -> {
            check(request.method == HttpMethod.Get)
            checkNotNull(request.url.parameters["q"])
            checkNotNull(request.url.parameters["page"])

            respond(
              content = responseString,
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

      val either = ktorRepoItemApi.searchRepoItems(
        term = "kmm",
        page = 1
      )

      assertEquals(
        json.decodeFromString(responseString),
        either.getOrThrow
      )
    }

  @Test
  fun `searchRepoItems returns a Left WHEN httpClient returns a failure response`() =
    runTest(testAppCoroutineDispatchers.testCoroutineDispatcher) {
      handlerChannel.trySend { request ->
        when (request.url.encodedPath) {
          "/search/repositories" -> {
            check(request.method == HttpMethod.Get)
            checkNotNull(request.url.parameters["q"])
            checkNotNull(request.url.parameters["page"])

            respond(
              "{}",
              status = HttpStatusCode.InternalServerError,
              headers = headersOf(
                HttpHeaders.ContentType,
                ContentType.Application.Json.toString()
              )
            )
          }
          else -> error("Unhandled request ${request.url}")
        }
      }

      val either = ktorRepoItemApi.searchRepoItems(
        term = "kmm",
        page = 1
      )

      either.leftValueOrThrow
    }
}
