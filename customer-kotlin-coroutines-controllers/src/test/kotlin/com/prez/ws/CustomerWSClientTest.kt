package com.prez.ws

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.prez.ws.model.Card
import com.prez.ws.model.Cards
import com.prez.ws.model.GetCustomerWSResponse
import com.prez.ws.model.Email
import com.prez.ws.model.File
import com.prez.ws.model.Misc
import com.prez.ws.model.NestedValue
import com.prez.ws.model.PersonalDetails
import com.prez.ws.model.PersonalInformation
import com.prez.ws.model.Photos
import com.prez.ws.model.Record
import com.prez.ws.model.Service
import com.prez.ws.model.Services
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.assertFailsWith

@Tag("integration")
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CustomerWSClientTest {

  companion object {

    lateinit var wireMockServer: WireMockServer

    @BeforeAll
    @JvmStatic
    fun beforeAll() {

      wireMockServer = WireMockServer(
        options()
          .dynamicPort()
          .usingFilesUnderClasspath("com/devoxx/ws")
      )

      wireMockServer.start()
      configureFor("localhost", wireMockServer.port()) // indispensable en reactif

      //OK Partial response
      wireMockServer.stubFor(
        get("/customers/partial-customer")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBodyFile("partialCustomer.json")
          )
      )

      //OK full response with customer infos and loyalty and passes
      wireMockServer.stubFor(
        get("/customers/full-customer")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBodyFile("fullCustomer.json")
          )
      )

      // 301
      wireMockServer.stubFor(
        get("/customers/301")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(301)
              .withHeader("Content-Type", "application/json")
              .withBody("{\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}")
          )
      )

      // bad request
      wireMockServer.stubFor(
        get("/customers/bad")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(400)
              .withHeader("Content-Type", "application/json")
              .withBody("{\"error\":\"Bad est un album de Mickael Jackson\"}")
          )
      )

      // 404
      wireMockServer.stubFor(
        get("/customers/unknown-id")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(404)
              .withHeader("Content-Type", "application/json")
          )
      )

      // connection reset by peer
      wireMockServer.stubFor(
        get("/customers/connection-reset-by-peer")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withFault(Fault.CONNECTION_RESET_BY_PEER)
          )
      )

      // RESPONSE FOR POST
      wireMockServer.stubFor(
        WireMock.post("/customers")
          .withHeader("Content-Type", EqualToPattern("application/json"))
          .withHeader("Accept", EqualToPattern("application/json"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBody("{\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}")
          )
      )
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      wireMockServer.stop()
    }
  }

  private var configuration = CustomerWSProperties(
    url = "http://localhost:" + wireMockServer.port() + "/customers"
  )

  private val webClient = WebClient.builder()
    .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
    .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
    .filter(basicAuthentication("user", "pwd"))
    .build()

  private val toTest = CustomerWSClient(configuration, webClient)

  @Test
  fun `getCustomer should return a customer when successful call to customer web service with a partial response`(): Unit =
    runBlocking {
      // Given && When
      val customerWSResponse = toTest.getCustomer("partial-customer")

      // Then
      assertThat(customerWSResponse).isNotNull
      val comparisonConf = RecursiveComparisonConfiguration()
      comparisonConf.ignoreCollectionOrderInFields("misc.records.map")
      assertThat(customerWSResponse).usingRecursiveComparison(comparisonConf)
        .ignoringExpectedNullFields().isEqualTo(
        GetCustomerWSResponse(
          id = "222748af-ba4b-4a58-91ce-817ab8454d33",
          personalDetails = PersonalDetails(
            email = Email(
              address = "root@themachine",
              default = true,
              confirmed = NestedValue(value = "CHECKED")
            ),
            cell = null
          ),
          personalInformation =
          PersonalInformation(
            civility = null,
            firstName = "Samantha",
            lastName = "Groves",
            alive = null,
            birthdate = null
          ),
          cards = Cards(
            listOf(
              Card(
                number = "001.548.25.MPPS",
                type = NestedValue(value = "LOYALTY"),
                ticketless = false,
                disableStatus = NestedValue(value = "000")
              )
            )
          ),
          services = Services(
            list = listOf(
              Service(
                name = NestedValue(value = "objectID"),
                status = NestedValue(value = "subscribed"),
                updatedTime = "2019-08-29T15:26:31Z"
              ),
              Service(
                name = NestedValue(value = "loyalty"),
                status = NestedValue(value = "_019875"),
                updatedTime = "2019-11-10T00:00:00Z"
              )
            )
          ),
          photos = Photos(
            file = File(
              id = "http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/222748af-ba4b-4a58-91ce-817ab8454d33/photos/file"
            )
          ),
          misc = listOf(
            Misc(
              type = NestedValue(value = "LOYALTY"),
              count = 1,
              hasMore = true,
              records = listOf(
                Record(
                  otherId = "001.548.25.MPPS",
                  type = NestedValue(value = "LOYALTY"),
                  map = listOf(
                    mapOf(
                      "key" to "loyalty_status",
                      "value" to "_019875"
                    ),
                    mapOf(
                      "key" to "some_key",
                      "value" to "some_value"
                    ),
                    mapOf(
                      "key" to "how_are_you_today",
                      "value" to "cold"
                    ),
                    mapOf(
                      "key" to "loyalty_number",
                      "value" to "001.548.25.MPPS"
                    ),
                    mapOf(
                      "key" to "old_product_code",
                      "value" to "FIDELITE"
                    ),
                    mapOf(
                      "key" to "loyalty_status_label",
                      "value" to "EMERAUDE"
                    ),
                    mapOf(
                      "key" to "validity_start",
                      "value" to "2012-03-05"
                    ),
                    mapOf(
                      "key" to "validity_end",
                      "value" to "2013-03-05"
                    ),
                    mapOf(
                      "key" to "status_d",
                      "value" to "2019-11-10"
                    ),
                    mapOf(
                      "key" to "disable_status",
                      "value" to "000"
                    )
                  )
                )
              )
            )
          )
        )
      )
    }

  @Test
  fun `getCustomer should raise a web service exception when response is 3xx`(): Unit = runBlocking {
    // Given && When
    val thrown = assertFailsWith<WebServiceException> { /* behind is a runCatching{} */
      toTest.getCustomer("301")
    }

    // Then
    assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY)
    assertThat(thrown.error.errorDescription)
      .isEqualTo("Unexpected response from the server while retrieving customer for customerId=301, " +
        "response={\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}")
  }

  @Test
  fun `getCustomer should raise a web service exception when response is 400`(): Unit = runBlocking {
    // Given && When
    val thrown = assertFailsWith<WebServiceException> { /* behind is a runCatching{} */
      toTest.getCustomer("bad")
    }

    // Then
    assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    assertThat(thrown.error.errorDescription)
      .isEqualTo("Unexpected response from the server while retrieving customer for customerId=bad," +
      " response={\"error\":\"Bad est un album de Mickael Jackson\"}")
  }

  @Test
  fun `getCustomer should return an empty customer when response is 404`() = runBlocking {
    // Given && When
    val customerInfo = toTest.getCustomer("unknown-id")

    // Then
    assertThat(customerInfo).isNull()
  }

  @Test
  fun `getCustomer should raise web service exception when response is not OK at all (CONNECTION_RESET_BY_PEER)`(): Unit =
    runBlocking {
      // Given && When
      val thrown = assertFailsWith<WebServiceException> { /* behind is a runCatching{} */
        toTest.getCustomer("connection-reset-by-peer")
      }

      // Then
      assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
      //because depending on your OS, the message is not always the same...
      assertThat(thrown.error.errorDescription)
        .contains("Unexpected error : ", "Connection reset")
    }

    // TODO add full customer test
}

