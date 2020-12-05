package com.prez.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.prez.model.LoyaltyStatus.B0B0B0;
import static com.prez.model.LoyaltyStatus.E0E0E0;
import static com.prez.model.PassType.FAMILY;
import static com.prez.model.PassType.YOUTH;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.prez.UsingMongoDBAndRedis;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.RailPass;
import com.prez.utils.FakeTokenGenerator;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("docker")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
@ContextConfiguration(initializers = {GetCustomerIntegrationTest.Initializer.class})
class GetCustomerIntegrationTest extends UsingMongoDBAndRedis {

  private static final WireMockRule wireMockServer = new WireMockRule(options()
      .dynamicPort()
      .usingFilesUnderClasspath("com/devoxx/ws")
  );

  private static final FakeTokenGenerator FAKE_TOKEN_GENERATOR = new FakeTokenGenerator("test-authorization-server");
  private static final String VALID_TOKEN =
      FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("subzero", 3600, "customer.read customer.write");

  @Autowired
  private WebTestClient client;
  @Autowired
  private ReactiveRedisTemplate<String, Customer> customerInfoRedisTemplate;

  @BeforeAll
  static void beforeAll() {
    wireMockServer.start();
    configureFor("localhost", wireMockServer.port()); // indispensable en reactif

    // partie Customer web service
    //ok
    stubFor(get("/customers/subzero")
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("fullCustomer.json")
        )
    );

    //bad request
    stubFor(get("/customers/badRequest")
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"code\":\"2012\",\"message\":\"Error\"}")
        )
    );

    //404
    stubFor(get("/customers/unknownCustomer")
        .willReturn(aResponse()
            .withStatus(404)
        )
    );

    // connection reset by peer
    stubFor(
        get("/customers/connectionLost")
            .willReturn(aResponse()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)
            )
    );

  }

  @AfterAll
  static void afterAll() {
    wireMockServer.shutdown();
  }

  @BeforeEach
  void beforeEach() {
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block();
    resetAllRequests(); //reset all requests registered by wiremock to be isolate each fullCustomer.json
  }

  @Test
  @DisplayName("GET customers should return 401 if not authenticated")
  void get_customers_should_return_401_if_not_authenticated() {
    client
        .get()
        .uri("/customers")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  @DisplayName("GET customers should return 401 if idToken is expired")
  void get_customers_should_return_401_if_idToken_is_expired() {
    //When
    final String expired = FAKE_TOKEN_GENERATOR.generateSignedToken(
        "expired",
        Date.from(Instant.now().minusSeconds(300)),
        "customer.read"
    );

    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + expired)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized();
  }

  @Test
  @DisplayName("GET customers should return customer info with both loyaltyProgram and railpasses if it is cached")
  void GET_customers_should_return_customer_info_with_both_loyaltyProgram_and_railpasses_if_it_is_cached() {
    //When
    customerInfoRedisTemplate.opsForValue().set(
        "Customer:subzero", Customer.builder()
            .customerId("subzero")
            .email("mission.impossible@connect.fr")
            .firstName("Jim")
            .lastName("Phelps")
            .birthDate(LocalDate.of(1952, 2, 29))
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("29090109625088082")
                .statusRefLabel("Grey")
                .status(E0E0E0)
                .validityStartDate(LocalDate.of(2019, 8, 12))
                .validityEndDate(LocalDate.of(2019, 8, 13))
                .build())
            .railPasses(singletonList(
                RailPass.builder()
                    .number("29090102420412755")
                    .typeRefLabel("So Young!")
                    .type(YOUTH)
                    .validityStartDate(LocalDate.of(2020, 4, 18))
                    .validityEndDate(LocalDate.of(2020, 3, 14))
                    .build()
            ))
            .build()
    ).block();

    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + VALID_TOKEN)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(
            "{\"customerId\":\"subzero\",\"firstName\":\"Jim\",\"lastName\":\"Phelps\",\"age\":68,\"email\":\"mission.impossible@connect.fr\",\"loyaltyProgram\":{\"number\":\"29090109625088082\",\"label\":\"Grey\",\"validityStartDate\":\"2019-08-12\",\"validityEndDate\":\"2019-08-13\"},\"railPasses\":[{\"number\":\"29090102420412755\",\"label\":\"So Young!\",\"validityStartDate\":\"2020-04-18\",\"validityEndDate\":\"2020-03-14\"}]}\n");

    verify(
        0, getRequestedFor(urlEqualTo("/customers/subzero"))
            .withHeader("Accept", new EqualToPattern("application/json"))
            .withHeader("Content-Type", new EqualToPattern("application/json"))
    );
  }

  @Test
  @DisplayName("GET customers should return customer info if it is not cached, retrieve it and cached it")
  void GET_customers_should_return_customer_info_if_it_is_not_cached_retrieve_it_and_cached_it() {
    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + VALID_TOKEN)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(
            "{\"customerId\":\"72f028e2-fbb8-48b3-b943-bf4daad961ed\"," +
                "\"firstName\":\"Elliot\"," +
                "\"lastName\":\"Alderson\"," +
                "\"phoneNumber\":\"0012125550179\"," +
                "\"email\":\"elliotalderson@protonmail.com\"," +
                "\"loyaltyProgram\": {" +
                "\"number\":\"ER28-0652\"," +
                "\"label\":\"PLATINIUM\"," +
                "\"validityStartDate\":\"2019-11-10\"," +
                "\"validityEndDate\":\"2020-11-09\"" +
                "}," +
                "\"railPasses\": [{" +
                "\"number\":\"07239107/23/91\"," +
                "\"label\":\"FAMILY PASS\"," +
                "\"validityStartDate\":\"2019-12-23\"," +
                "\"validityEndDate\":\"2021-12-23\"" +
                "},{" +
                "\"number\":\"29090113600311527\"," +
                "\"label\":\"FAMILY PASS\"," +
                "\"validityStartDate\":\"2018-12-23\"," +
                "\"validityEndDate\":\"2019-12-23\"" +
                "}]" +
            "}");

    //wiremock
    verify(
        getRequestedFor(urlEqualTo("/customers/subzero"))
            .withHeader("Accept", new EqualToPattern("application/json"))
            .withHeader("Content-Type", new EqualToPattern("application/json"))
    );

    //redis
    final Duration ttl = customerInfoRedisTemplate.getExpire("Customer:72f028e2-fbb8-48b3-b943-bf4daad961ed").block();
    final Customer savedCustomer =
        customerInfoRedisTemplate.opsForValue().get("Customer:72f028e2-fbb8-48b3-b943-bf4daad961ed").block();

    assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3));
    assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(10));
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("72f028e2-fbb8-48b3-b943-bf4daad961ed")
            .email("elliotalderson@protonmail.com")
            .firstName("Elliot")
            .lastName("Alderson")
            .birthDate(LocalDate.of(1986,9,17))
            .phoneNumber("0012125550179")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("ER28-0652")
                .statusRefLabel("PLATINIUM")
                .status(B0B0B0)
                .validityStartDate(LocalDate.of(2019, 11, 10))
                .validityEndDate(LocalDate.of(2020, 11, 9))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("07239107/23/91")
                    .typeRefLabel("FAMILY PASS")
                    .type(FAMILY)
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build(),
                RailPass.builder()
                    .number("29090113600311527")
                    .typeRefLabel("FAMILY PASS")
                    .type(FAMILY)
                    .validityStartDate(LocalDate.of(2018, 12, 23))
                    .validityEndDate(LocalDate.of(2019, 12, 23))
                    .build()
            ))
            .build()
    );
  }

  @Test
  @DisplayName("GET customers should return 404 if customer info has not been found anywhere")
  void GET_customers_should_return_404_if_customer_info_has_not_been_found_anywhere() {
    final String unknown = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("unknownCustomer", 3600, "customer.read");
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + unknown)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody().json("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given customer id=unknownCustomer\"}");
  }

  @Test
  @DisplayName("GET customer should return 500 if the line was cut by customer web service (connection-reset-by-peer)")
  void GET_customers_should_return_500_if_the_line_was_cut_by_customer_web_service() {
    //When
    final String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("connectionLost", 3600, "customer.read");

    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
        .expectBody().json("{\"code\":\"UNEXPECTED_ERROR\",\"message\":\"Something horribly wrong happened, I could tell you what but then I’d have to kill you.\"}");

    //because depending on your OS, the message is not always the same... ...{"code":"CUSTOMER_WS_GET_CUSTOMER_ERROR","message":"Unexpected response from the server while retrieving customer for customerId=connectionLost
  }

  @Test
  @DisplayName("GET customers should return 400 if there is an exception when calling Customer web service")
  void GET_customers_should_return_400_if_there_is_an_exception_when_calling_Customer_web_service() {
    //When
    final String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("badRequest", 3600, "customer.read");

    //Test & Assert
    client
        .get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody().json(
        "{\"code\":\"CUSTOMER_WS_GET_CUSTOMER_ERROR\",\"message\":\"Unexpected response from the server while retrieving customer for customerId=badRequest, response={\\\"code\\\":\\\"2012\\\",\\\"message\\\":\\\"Error\\\"}\"}\n"
    );
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          applicationContext,
          "customer.ws.url=http://localhost:" + wireMockServer.port() + "/customers"
      );
    }
  }
}
