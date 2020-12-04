package com.prez.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import com.prez.UsingMongoDBAndRedis
import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus.B0B0B0
import com.prez.model.LoyaltyStatus.E0E0E0
import com.prez.model.PassType
import com.prez.model.PassType.YOUTH
import com.prez.model.RailPass
import com.prez.utils.FakeTokenGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Date

@Tag("docker")
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["test"])
@ContextConfiguration(initializers = [GetCustomerIntegrationTest.Companion.Initializer::class])
internal class GetCustomerIntegrationTest(
  @Autowired val client: WebTestClient,
  @Autowired val customerInfoRedisTemplate: ReactiveRedisTemplate<String, Customer>
) : UsingMongoDBAndRedis() {

  companion object {

    lateinit var wireMockServer: WireMockServer

    private val fakeTokenGenerator: FakeTokenGenerator = FakeTokenGenerator("test-authorization-server")
    private val validToken =
      fakeTokenGenerator.generateNotExpiredSignedToken("subzero", 3600, "customer.read customer.write")

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      wireMockServer = WireMockServer(
        WireMockConfiguration.options()
          .dynamicPort()
          .usingFilesUnderClasspath("com/devoxx/ws")
      )

      wireMockServer.start()
      configureFor("localhost", wireMockServer.port()) // indispensable en reactif

      // partie Customer web service
      //ok
      stubFor(
        get("/customers/subzero")
          .willReturn(
            aResponse()
              .withStatus(200)
              .withHeader("Content-Type", "application/json")
              .withBodyFile("fullCustomer.json")
          )
      )

      //bad request
      stubFor(
        get("/customers/badRequest")
          .willReturn(
            aResponse()
              .withStatus(400)
              .withHeader("Content-Type", "application/json")
              .withBody("""{"code":"2012","message":"Error"}""")
          )
      )

      //404
      stubFor(
        get("/customers/unknownCustomer")
          .willReturn(
            aResponse()
              .withStatus(404)
          )
      )

      // connection reset by peer
      stubFor(
        get("/customers/connectionLost")
          .willReturn(
            aResponse()
              .withFault(Fault.CONNECTION_RESET_BY_PEER)
          )
      )
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      wireMockServer.stop()
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
      override fun initialize(applicationContext: ConfigurableApplicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          applicationContext,
          "customer.ws.url=http://localhost:" + wireMockServer.port() + "/customers"
        )
      }
    }
  }

  @BeforeEach
  internal fun beforeEach() {
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block()
    resetAllRequests() //reset all requests registered by wiremock to be isolate each fullCustomer.json
  }

  @Test
  fun `GET customers should return 401 if not authentified`() {
    //When, Test & Assert
    client
      .get()
      .uri("/customers")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `GET customers should return 401 if idToken is expired`() {
    //When
    val expired = fakeTokenGenerator.generateSignedToken(
      "expired",
      Date.from(Instant.now().minusSeconds(300)),
      "customer.read"
    )

    //Test & Assert
    client
      .get()
      .uri("/customers")
      .header("Authorization", "Bearer $expired")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `GET customers should return customer info with both loyaltyProgram and railpasses if it is cached`() {
    //When
    customerInfoRedisTemplate.opsForValue().set(
      "Customer:subzero", Customer(
        customerId = "subzero",
        email = "mission.impossible@connect.fr",
        firstName = "Jim",
        lastName = "Phelps",
        birthDate = LocalDate.of(1952, 2, 29),
        phoneNumber = null,
        loyaltyProgram = LoyaltyProgram(
          number = "29090109625088082",
          statusRefLabel = "Grey",
          status = E0E0E0,
          validityStartDate = LocalDate.of(2019, 8, 12),
          validityEndDate = LocalDate.of(2019, 8, 13)
        ),
        railPasses = listOf(
          RailPass(
            number = "29090102420412755",
            typeRefLabel = "So Young!",
            type = YOUTH,
            validityStartDate = LocalDate.of(2020, 4, 18),
            validityEndDate = LocalDate.of(2020, 3, 14)
          )
        )
      )
    ).block()

    //Test & Assert
    client
      .get()
      .uri("/customers")
      .header("Authorization", "Bearer $validToken")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("""
        {
          "customerId":"subzero",
          "firstName":"Jim",
          "lastName":"Phelps",
          "age":68,
          "email":"mission.impossible@connect.fr",
          "loyaltyProgram": {
            "number":"29090109625088082",
            "label":"Grey",
            "validityStartDate":"2019-08-12",
            "validityEndDate":"2019-08-13"
          },
          "railPasses":[{
              "number":"29090102420412755",
              "label":"So Young!",
              "validityStartDate":"2020-04-18",
              "validityEndDate":"2020-03-14"
          }]
        }
        """.trimIndent())

    verify(
      0, getRequestedFor(urlEqualTo("/customers/subzero"))
        .withHeader("Accept", EqualToPattern("application/json"))
        .withHeader("Content-Type", EqualToPattern("application/json"))
    )
  }

  @Test
  fun `GET customers should return customer info if it is not cached, retrieve it and cached it`() {
    //Test & Assert
    client
      .get()
      .uri("/customers")
      .header("Authorization", "Bearer $validToken")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("""
        {
          "customerId":"72f028e2-fbb8-48b3-b943-bf4daad961ed",
          "firstName":"Elliot",
          "lastName":"Alderson",
          "phoneNumber":"0012125550179",
          "email":"elliotalderson@protonmail.com"
        }
        """.trimIndent())


    //wiremock
    verify(
      getRequestedFor(urlEqualTo("/customers/subzero"))
        .withHeader("Accept", EqualToPattern("application/json"))
        .withHeader("Content-Type", EqualToPattern("application/json"))
    )

    //redis
    val ttl = customerInfoRedisTemplate.getExpire("Customer:72f028e2-fbb8-48b3-b943-bf4daad961ed").block()
    val savedCustomer = customerInfoRedisTemplate.opsForValue().get("Customer:72f028e2-fbb8-48b3-b943-bf4daad961ed").block()
    assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3))
    assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(10))
    val compConfig = RecursiveComparisonConfiguration()
    assertThat(savedCustomer).usingRecursiveComparison(compConfig)
            .isEqualTo(
      Customer(
        customerId = "72f028e2-fbb8-48b3-b943-bf4daad961ed",
        email = "elliotalderson@protonmail.com",
        firstName = "Elliot",
        lastName = "Alderson",
        birthDate = LocalDate.of(1986,9,17),
        phoneNumber = "0012125550179",
        loyaltyProgram = LoyaltyProgram("ER28-0652", B0B0B0, "PLATINIUM",
                LocalDate.of(2019, 11, 10), LocalDate.of(2020,11,9)),
        railPasses = listOf(
                RailPass("07239107/23/91", PassType.FAMILY, "FAMILY PASS",
                  LocalDate.of(2019,12,23), LocalDate.of(2021,12,23)),
                RailPass("29090113600311527", PassType.FAMILY, "FAMILY PASS",
                        LocalDate.of(2018,12,23), LocalDate.of(2019,12,23))
        )
      )
    )
  }

  @Test
  fun `GET customers should return 404 if customer info has not been found anywhere`() {
    val unknown = fakeTokenGenerator.generateNotExpiredSignedToken("unknownCustomer", 3600, "customer.read")
    client
      .get()
      .uri("/customers")
      .header("Authorization", "Bearer $unknown")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectBody().json("""
        {
          "code":"NOT_FOUND",
          "message":"No result for the given customer id=unknownCustomer"
        }
        """.trimIndent())
  }

  @Test
  fun `GET customers should return 500 if the line was cut by customer web service`() {
    //When
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("connectionLost", 3600, "customer.read")

    //Test & Assert
    val returnedResult = client
      .get()
      .uri("/customers")
      .header("Authorization", "Bearer $accessToken")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
      .expectBody().returnResult()

    //because depending on your OS, the message is not always the same... ...{"code":"CUSTOMER_WS_GET_CUSTOMER_ERROR","message":"Unexpected response from the server while retrieving customer for customerId=connectionLost
    assertThat(String(returnedResult.responseBody))
        .contains("""{"code":"CUSTOMER_WS_GET_CUSTOMER_ERROR","message":"Unexpected""",
            "for customerId=connectionLost")
  }

  @Test
  fun `GET customers should return 400 if there is an exception when calling Customer web service`() {
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("badRequest", 3600, "customer.read")

    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer $accessToken")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        // https://discuss.kotlinlang.org/t/type-interference-issue-with-the-webflux-webtestclient-and-kotlin/3880
        .expectBody().json(
            "{\"code\":\"CUSTOMER_WS_GET_CUSTOMER_ERROR\",\"message\":\"Unexpected response from the server while retrieving customer for customerId=badRequest, response={\\\"code\\\":\\\"2012\\\",\\\"message\\\":\\\"Error\\\"}\"}"
        )
  }
}