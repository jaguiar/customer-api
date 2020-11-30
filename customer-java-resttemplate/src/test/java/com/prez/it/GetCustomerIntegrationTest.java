package com.prez.it;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
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
import static org.springframework.http.HttpMethod.GET;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.prez.UsingMongoDBAndRedis;
import com.prez.api.dto.CustomerResponse;
import com.prez.api.dto.LoyaltyProgramResponse;
import com.prez.api.dto.RailPassResponse;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.RailPass;
import com.prez.utils.FakeTokenGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@ContextConfiguration(initializers = {GetCustomerIntegrationTest.Initializer.class})
class GetCustomerIntegrationTest extends UsingMongoDBAndRedis {

  private static final WireMockRule wireMockServer = new WireMockRule(options()
      .dynamicPort()
      .usingFilesUnderClasspath("com/devoxx/ws")
  );
  private static final FakeTokenGenerator FAKE_TOKEN_GENERATOR = new FakeTokenGenerator("test-authorization-server");
  private static final String VALID_TOKEN =
      FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("subzero", 3600, "customer.read customer.write");

  @LocalServerPort
  private int localServerPort;
  @Autowired
  private KeyValueAdapter customerInfoRedisTemplate;
  @Autowired
  private MongoOperations mongoOperations;

  private TestRestTemplate restTemplate;
  private HttpHeaders httpHeaders;
  private String baseUrl;

  @BeforeAll
  static void setUp() {

    wireMockServer.start();
    configureFor("localhost", wireMockServer.port());

    // partie Customer web service
    //ok
    wireMockServer.stubFor(get("/customers/subzero")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("fullCustomer.json"))
    );

    // bad request
    wireMockServer.stubFor(get("/customers/badRequest")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(400)
            .withHeader("Content-Type", "application/json")
            .withBody("Bad est un album de Mickael Jackson")
        ));

    // 404
    wireMockServer.stubFor(get("/customers/unknownCustomer")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("Content-Type", "application/json"))
    );

    //500
    // connection reset by peer
    wireMockServer.stubFor(get("/customers/connectionLost")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withFault(Fault.CONNECTION_RESET_BY_PEER))
    );

    wireMockServer.stubFor(get("/customers/boom")
        .withHeader("Content-Type", new EqualToPattern("application/json"))
        .withHeader("Accept", new EqualToPattern("application/json"))
        .willReturn(aResponse()
            .withStatus(500))
    );

  }

  @AfterAll
  static void cleanUp() {
    wireMockServer.shutdown();
  }

  @BeforeEach
  void beforeEach() {
    baseUrl = "http://localhost:" + localServerPort;

    restTemplate = new TestRestTemplate();

    httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.setAccept(singletonList(MediaType.APPLICATION_JSON));
  }

  @AfterEach
  void afterEach() {
    customerInfoRedisTemplate.deleteAllOf("customer");
    mongoOperations.dropCollection("preferences");
    resetAllRequests(); //reset all requests registered by wiremock to be isolate each fullCustomer.json
  }

  @Test
  @DisplayName("GET customers should return 401 if not authenticated")
  void get_customers_should_return_401_if_not_authenticated() {
    // When
    ResponseEntity<Customer> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), Customer.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("GET customers should return 401 if idToken is expired")
  void get_customers_should_return_401_if_idToken_is_expired() {
    // Given
    String expiredToken = FAKE_TOKEN_GENERATOR
        .generateSignedToken("expired", Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)),
            "customer.read customer.write");
    httpHeaders.set("Authorization", "Bearer " + expiredToken);

    // When
    ResponseEntity<Customer> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), Customer.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("GET customers should return customer info with both loyaltyProgram and railpasses if it is cached")
  void GET_customers_should_return_customer_info_with_both_loyaltyProgram_and_railpasses_if_it_is_cached() {
    // Given a customer in cache
    Customer subzero = Customer.builder()
        .customerId("subzero")
        .email("mission.impossible@connect.fr")
        .firstName("Jim")
        .lastName("Phelps")
        .birthDate(LocalDate.of(1952, 2, 29))
        .phoneNumber(null)
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
        .build();
    customerInfoRedisTemplate.put("subzero", subzero, "customer");

    httpHeaders.set("Authorization", "Bearer " + VALID_TOKEN);

    // When
    ResponseEntity<CustomerResponse> responseEntity = this.restTemplate
        .exchange(baseUrl + "/customers", GET, new HttpEntity<>(httpHeaders), CustomerResponse.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).usingRecursiveComparison()
        .ignoringActualNullFields()
        .isEqualTo(CustomerResponse.builder()
            .customerId("subzero")
            .firstName("Jim")
            .lastName("Phelps")
            .age(Period.between(LocalDate.of(1952, 2, 29), LocalDate.now()).getYears())
            .email("mission.impossible@connect.fr")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgramResponse.builder()
                .number("29090109625088082")
                .label("Grey")
                .validityStartDate(LocalDate.of(2019, 8, 12))
                .validityEndDate(LocalDate.of(2019, 8, 13))
                .build())
            .railPasses(singletonList(RailPassResponse.builder()
                .number("29090102420412755")
                .label("So Young!")
                .validityStartDate(LocalDate.of(2020, 4, 18))
                .validityEndDate(LocalDate.of(2020, 3, 14))
                .build())).build());
  }

  @Test
  @DisplayName("GET customers should return customer info if it is not cached, retrieve it and cached it")
  void GET_customers_should_return_customer_info_if_it_is_not_cached_retrieve_it_and_cached_it() {
    // Given
    httpHeaders.set("Authorization", "Bearer " + VALID_TOKEN);

    // When
    ResponseEntity<CustomerResponse> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), CustomerResponse.class);

    // Then
    assertThat(responseEntity.getBody()).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(
        CustomerResponse.builder()
            .customerId("72f028e2-fbb8-48b3-b943-bf4daad961ed")
            .firstName("Elliot")
            .lastName("Alderson")
            .phoneNumber("0012125550179")
            .email("elliotalderson@protonmail.com")
            .age(Period.between(LocalDate.of(1986, 9, 17), LocalDate.now()).getYears())
            .loyaltyProgram(LoyaltyProgramResponse.builder()
                .number("ER28-0652")
                .label("PLATINIUM")
                .validityStartDate(LocalDate.of(2019, 11, 10))
                .validityEndDate(LocalDate.of(2020, 11, 9))
                .build())
            .railPasses(asList(RailPassResponse.builder()
                    .number("07239107/23/91")
                    .label("FAMILY PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build(),
                RailPassResponse.builder()
                    .number("29090113600311527")
                    .label("FAMILY PASS")
                    .validityStartDate(LocalDate.of(2018, 12, 23))
                    .validityEndDate(LocalDate.of(2019, 12, 23))
                    .build())).build());

    //wiremock
    verify(
        getRequestedFor(urlEqualTo("/customers/subzero"))
            .withHeader("Accept", new EqualToPattern("application/json"))
            .withHeader("Content-Type", new EqualToPattern("application/json"))
    );

    // redis
    // FIXME TTL ?
    // final Duration ttl = customerInfoRedisTemplate.getExpire("72f028e2-fbb8-48b3-b943-bf4daad961ed", "customer");
    // assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3));
    // assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(10));

    final Customer savedCustomer = (Customer) customerInfoRedisTemplate.get("72f028e2-fbb8-48b3-b943-bf4daad961ed", "customer");
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(Customer.builder()
        .customerId("72f028e2-fbb8-48b3-b943-bf4daad961ed")
        .firstName("Elliot")
        .lastName("Alderson")
        .phoneNumber("0012125550179")
        .email("elliotalderson@protonmail.com")
        .birthDate(LocalDate.of(1986, 9, 17))
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("ER28-0652")
            .statusRefLabel("PLATINIUM")
            .status(B0B0B0)
            .validityStartDate(LocalDate.of(2019, 11, 10))
            .validityEndDate(LocalDate.of(2020, 11, 9))
            .build())
        .railPasses(asList(RailPass.builder()
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
                .build())).build());
  }

  @Test
  @DisplayName("GET customers should return 404 if customer info has not been found anywhere")
  void GET_customers_should_return_404_if_customer_info_has_not_been_found_anywhere() {
    // Given
    String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("unknownCustomer", 3600, "customer.read");
    httpHeaders.set("Authorization", "Bearer " + accessToken);

    // When
    ResponseEntity<Customer> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), Customer.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("GET customer should return 500 if the line was cut by customer web service (connection-reset-by-peer)")
  void GET_customers_should_return_500_if_the_line_was_cut_by_customer_web_service() {
    // Given
    String accessToken =
        FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("connectionLost", 3600, "customer.read customer.write");
    httpHeaders.set("Authorization", "Bearer " + accessToken);

    // When
    ResponseEntity<String> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), String.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(responseEntity.getBody()).isEqualTo("{\"code\":\"UNEXPECTED_ERROR\"," +
        "\"message\":\"Something horribly wrong happened, I could tell you what but then I’d have to kill you.\"}");
  }

  @Test
  @DisplayName("GET customers should return 400 if there is an exception when calling Customer web service")
  void GET_customers_should_return_400_if_there_is_an_exception_when_calling_Customer_web_service() {
    // Given
    String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("badRequest", 3600, "customer.read");
    httpHeaders.set("Authorization", "Bearer " + accessToken);

    // When
    ResponseEntity<Customer> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), Customer.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("GET customer should return 500 internal server error when nothing goes as planned")
  void shouldFail_whenNothingGoesAsPlanned() {
    // Given
    String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("boom", 3600, "customer.read customer.write");
    httpHeaders.set("Authorization", "Bearer " + accessToken);

    // When
    ResponseEntity<String> responseEntity = this.restTemplate.exchange(baseUrl + "/customers", GET,
        new HttpEntity<>(httpHeaders), String.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(responseEntity.getBody()).isEqualTo("{\"code\":\"CUSTOMER_WS_UNEXPECTED_ERROR\"," +
        "\"message\":\"Unexpected response from the server.\"}");
  }

  // TODO add examples of timeout, etc. + cas pour tester les exceptions handlers

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
