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
    fun `getCustomer should return a customer when successful call to customer web service with a partial response`() {
        // Given && When
        val customerWSResponse = toTest.getCustomer("partial-customer").block()

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
    fun `getCustomer should raise a web service exception when response is 3xx`() {
        // Given && When
        val thrown = assertFailsWith<WebServiceException> {
            toTest.getCustomer("301").block()
        }

        // Then
        assertThat(thrown).isNotNull
        assertThat(thrown).isInstanceOf(WebServiceException::class.java)
        assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.MOVED_PERMANENTLY)
        assertThat(thrown.error.errorDescription)
                .isEqualTo("Unexpected response from the server while retrieving customer for customerId=301, " +
                        "response={\"email\":\"awsome@user.com\",\"firstName\":\"Awsome\",\"lastName\":\"User\"}")
    }

    @Test
    fun `getCustomer should raise a web service exception when response is 400`() {
        // Given && When
        val thrown = assertFailsWith<WebServiceException> {
            toTest.getCustomer("bad").block()
        }

        // Then
        assertThat(thrown).isNotNull
        assertThat(thrown).isInstanceOf(WebServiceException::class.java)
        assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(thrown.error.errorDescription).isEqualTo("Unexpected response from the server while retrieving customer for customerId=bad, response={\"error\":\"Bad est un album de Mickael Jackson\"}")
    }

    @Test
    fun `getCustomer should return an empty customer when response is 404`() {
        // Given && When
        val customerInfo = toTest.getCustomer("unknown-id").block()

        // Then
        assertThat(customerInfo).isNull()
    }

    @Test
    fun `getCustomer should raise web service exception when response is not OK at all (CONNECTION_RESET_BY_PEER)`() {
        // Given && When
        val thrown = assertFailsWith<WebServiceException> { toTest.getCustomer("connection-reset-by-peer").block() }

        // Then
        assertThat(thrown).isNotNull
        assertThat(thrown).isInstanceOf(WebServiceException::class.java)
        assertThat(thrown.httpStatusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        //because depending on your OS, the message is not always the same...
        assertThat(thrown.error.errorDescription)
            .contains("Unexpected error : ", "Connection reset")
    }

    @Test
    fun `getCustomer should return a customer when successful call to customer web service with a full response`() {
            // Given && When
            val customerWSResponse = toTest.getCustomer("full-customer")

            // Then
            assertThat(customerWSResponse).isNotNull
            val comparisonConf = RecursiveComparisonConfiguration()
            comparisonConf.ignoreCollectionOrderInFields("misc.records.map")
            assertThat(customerWSResponse).usingRecursiveComparison(comparisonConf)
                .ignoringExpectedNullFields().isEqualTo(
                    GetCustomerWSResponse(
                        id = "72f028e2-fbb8-48b3-b943-bf4daad961ed",
                        personalDetails = PersonalDetails(
                            email = Email(
                                address = "elliotalderson@protonmail.com",
                                default = true,
                                confirmed = NestedValue(value = "CHECKED")
                            ),
                            cell = null
                        ),
                        personalInformation =
                        PersonalInformation(
                            civility = null,
                            firstName = "Elliot",
                            lastName = "Alderson",
                            alive = null,
                            birthdate = null
                        ),
                        cards = Cards(
                            listOf(
                                Card(
                                    number = "29090108600311527",
                                    type = NestedValue(value = "WEIRD_VALUE"),
                                    ticketless = true,
                                    disableStatus = NestedValue(value = "000")
                                ),
                                Card(
                                    number = "ER28-0652",
                                    type = NestedValue(value = "LOYALTY"),
                                    ticketless = true,
                                    disableStatus = NestedValue(value = "000")
                                ),
                                Card(
                                    number = "07239107/23/91",
                                    type = NestedValue(value = "FAMILY"),
                                    ticketless = true,
                                    disableStatus = NestedValue(value = "000")
                                )
                            )
                        ),
                        services = Services(
                            list = listOf(
                                Service(
                                    name = NestedValue(value = "fda"),
                                    status = NestedValue(value = "subscribed"),
                                    updatedTime = "2019-08-29T15:26:31Z"
                                ),
                                Service(
                                    name = NestedValue(value = "loyalty"),
                                    status = NestedValue(value = "B0B0B0"),
                                    updatedTime = "2019-11-10T00:00:00Z"
                                ),
                                Service(
                                    name = NestedValue(value = "dematerialization"),
                                    status = NestedValue(value = "subscribed"),
                                    updatedTime = "2019-08-29T15:28:09Z"
                                ),
                                Service(
                                    name = NestedValue(value = "photo"),
                                    status = NestedValue(value = "subscribed"),
                                    updatedTime = "2019-08-29T15:28:06Z"
                                )
                            )
                        ),
                        photos = Photos(
                            file = File(
                                id = "http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/72f028e2-fbb8-48b3-b943-bf4daad961ed/photos/file"
                            )
                        ),
                        misc = listOf(
                            Misc(
                                type = NestedValue(value = "LOYALTY"),
                                count = 1,
                                hasMore = true,
                                records = listOf(
                                    Record(
                                        otherId = "ER28-0652",
                                        type = NestedValue(value = "LOYALTY"),
                                        map = listOf(
                                            mapOf(
                                                "key" to "some_key",
                                                "value" to "some_value"
                                            ),
                                            mapOf(
                                                "key" to "loyalty_status_label",
                                                "value" to "PLATINIUM"
                                            ),
                                            mapOf(
                                                "key" to "status_d",
                                                "value" to "2019-11-10"
                                            ),
                                            mapOf(
                                                "key" to "how_are_you_today",
                                                "value" to "delusional"
                                            ),
                                            mapOf(
                                                "key" to "validity_end",
                                                "value" to "2020-11-09"
                                            ),
                                            mapOf(
                                                "key" to "loyalty_number",
                                                "value" to "ER28-0652"
                                            ),
                                            mapOf(
                                                "key" to "disable_status",
                                                "value" to "000"
                                            ),
                                            mapOf(
                                                "key" to "old_product_code",
                                                "value" to "FIDELITE"
                                            ),
                                            mapOf(
                                                "key" to "validity_start",
                                                "value" to "2019-11-10"
                                            ),
                                            mapOf(
                                                "key" to "loyalty_status",
                                                "value" to "B0B0B0"
                                            )
                                        )
                                    )
                                )
                            ),
                            Misc(
                                type = NestedValue(value = "PASS"),
                                count = 2,
                                hasMore = false,
                                records = listOf(
                                    Record(
                                        otherId = "07239107/23/91",
                                        type = NestedValue(value = "PASS"),
                                        map = listOf(
                                            mapOf(
                                                "key" to "pass_number",
                                                "value" to "07239107/23/91"
                                            ),
                                            mapOf(
                                                "key" to "some_other_key",
                                                "value" to "for_no_reason"
                                            ),
                                            mapOf(
                                                "key" to "pass_validity_end",
                                                "value" to "2021-12-23"
                                            ),
                                            mapOf(
                                                "key" to "sous_type",
                                                "value" to "PASS_QUI_S_ACHETE"
                                            ),
                                            mapOf(
                                                "key" to "old_pass_label",
                                                "value" to "Pass Famille"
                                            ),
                                            mapOf(
                                                "key" to "pass_is_active",
                                                "value" to "000"
                                            ),
                                            mapOf(
                                                "key" to "some_reference",
                                                "value" to "UWVDJW"
                                            ),
                                            mapOf(
                                                "key" to "pass_label",
                                                "value" to "FAMILY PASS"
                                            ),
                                            mapOf(
                                                "key" to "some_date_key",
                                                "value" to "2021-12-23"
                                            ),
                                            mapOf(
                                                "key" to "new_product_code",
                                                "value" to "FAMILY"
                                            ),
                                            mapOf(
                                                "key" to "pass_validity_start",
                                                "value" to "2019-12-23"
                                            )

                                        )
                                    ),
                                    Record(
                                        otherId = "29090113600311527",
                                        type = NestedValue(value = "PASS"),
                                        map = listOf(
                                            mapOf(
                                                "key" to "pass_number",
                                                "value" to "29090113600311527"
                                            ),
                                            mapOf(
                                                "key" to "some_other_key",
                                                "value" to "for_no_reason"
                                            ),
                                            mapOf(
                                                "key" to "pass_validity_end",
                                                "value" to "2019-12-23"
                                            ),
                                            mapOf(
                                                "key" to "sous_type",
                                                "value" to "PASS_QUI_S_ACHETE"
                                            ),
                                            mapOf(
                                                "key" to "old_pass_label",
                                                "value" to "Pass Famille"
                                            ),
                                            mapOf(
                                                "key" to "pass_is_active",
                                                "value" to "000"
                                            ),
                                            mapOf(
                                                "key" to "some_reference",
                                                "value" to "ZZWWEE"
                                            ),
                                            mapOf(
                                                "key" to "pass_label",
                                                "value" to "FAMILY PASS"
                                            ),
                                            mapOf(
                                                "key" to "some_date_key",
                                                "value" to "2020-12-23"
                                            ),
                                            mapOf(
                                                "key" to "new_product_code",
                                                "value" to "FAMILY"
                                            ),
                                            mapOf(
                                                "key" to "pass_validity_start",
                                                "value" to "2018-12-23"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
        }


}

