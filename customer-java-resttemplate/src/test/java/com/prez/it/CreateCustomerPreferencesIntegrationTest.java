package com.prez.it;

import static com.prez.model.SeatPreference.NO_PREFERENCE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

import com.prez.Application;
import com.prez.UsingMongoDBAndRedis;
import com.prez.model.CustomerPreferences;
import com.prez.utils.FakeTokenGenerator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@Tag("docker")
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
class CreateCustomerPreferencesIntegrationTest extends UsingMongoDBAndRedis {

  private static final String CUSTOMERS_PREFERENCES_ENDPOINT = "/customers/preferences";

  @LocalServerPort
  private int localServerPort;
  @Autowired
  private KeyValueAdapter redisKeyspace;
  @Autowired
  private MongoOperations mongoOperations;

  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");
  private TestRestTemplate restTemplate;
  private HttpHeaders httpHeaders;
  private String baseUrl;

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
    redisKeyspace.deleteAllOf("customer");
    mongoOperations.dropCollection("preferences");
  }

  @Test
  @DisplayName("should return Bad Request when missing mandatory fields")
  void shouldReturnBadRequest_whenMissingMandatoryFields() throws URISyntaxException {
    // Given
    String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("cached", 3600, "customer.write");
    httpHeaders.set("Authorization", "Bearer " + validToken);

    // When
    String toCreate = "{"
        + "\"seatPreference\": \"NEAR_WINDOW\""
        + "}";
    RequestEntity<Object> requestEntity = new RequestEntity<>(toCreate, httpHeaders, POST, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<String> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, POST, requestEntity, String.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody()).startsWith("{\"code\":\"VALIDATION_ERROR\","
        + "\"message\":\"2 error(s) while validating createCustomerPreferencesRequest : ");
    assertThat(responseEntity.getBody()).contains("The profile name is missing");
    assertThat(responseEntity.getBody()).contains("The class preference is missing");

  }

  @Test
  @DisplayName("should return Bad Request when invalid input values")
  void shouldReturnBadRequest_whenInvalidInputValues() throws URISyntaxException {
    // Given
    String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("cached", 3600, "customer.write");
    httpHeaders.set("Authorization", "Bearer " + validToken);

    // When
    String toCreate = "{"
        + "\"seatPreference\":\"NEAR_CORRIDOR\","
        + "\"classPreference\":23,"
        + "\"profileName\":\"Trotro*\","
        + "\"language\":\"meh\""
        + "}";
    RequestEntity<Object> requestEntity = new RequestEntity<>(toCreate, httpHeaders, POST, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<String> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, POST, requestEntity, String.class);

    // Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(responseEntity.getBody()).startsWith("{\"code\":\"VALIDATION_ERROR\","
        + "\"message\":\"3 error(s) while validating createCustomerPreferencesRequest : ");
    assertThat(responseEntity.getBody()).contains("Max value for class preference is 2");
    assertThat(responseEntity.getBody()).contains("The profile name contains forbidden characters");
    assertThat(responseEntity.getBody()).contains("The language is not valid. Accepted languages are : fr,de,es,en,it,pt");
  }

  @Test
  @DisplayName("should return 403 forbidden when not authenticated user")
  void createCustomerPreferences_shouldReturn403_whenUserNotAuthenticated() throws Exception {
    // Given no authentication
    String validRequest = "{"
        + "\"seatPreference\":\"NEAR_WINDOW\","
        + "\"classPreference\":2,"
        + "\"profileName\":\"PasAssezPasAssez\","
        + "\"language\":\"fr\""
        + "}";

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(validRequest, httpHeaders, POST,
        new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferences> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, POST, requestEntity,
            CustomerPreferences.class);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("should return 403 forbidden when authenticated user with insufficient privileges")
  void createCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() throws Exception {
    // Given
    String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("cached", 3600, "customer.read");
    httpHeaders.set("Authorization", "Bearer " + validToken);
    String validRequest = "{"
        + "\"seatPreference\":\"NEAR_WINDOW\","
        + "\"classPreference\":2,"
        + "\"profileName\":\"PasAssezPasAssez\","
        + "\"language\":\"fr\""
        + "}";

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(validRequest, httpHeaders, POST,
        new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferences> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, POST, requestEntity,
            CustomerPreferences.class);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("should return created preferences when customer preferences successfully created for authorized user with valid input")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid()
      throws Exception {
    // Given
    final String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write");
    httpHeaders.set("Authorization", "Bearer " + validToken);
    String validRequest = "{"
        + "\"seatPreference\":\"NO_PREFERENCE\","
        + "\"classPreference\":1,"
        + "\"profileName\":\"Trotro\","
        + "\"language\":\"fr\""
        + "}";

    RequestEntity<Object> requestEntity = new RequestEntity<>(validRequest, httpHeaders, POST,
        new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferences> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, POST, requestEntity,
            CustomerPreferences.class);

    // When && Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    RecursiveComparisonConfiguration comparisonConfig = new RecursiveComparisonConfiguration();
    comparisonConfig.ignoreFields("id");
    assertThat(responseEntity.getBody().getId()).isNotNull();
    assertThat(responseEntity.getBody())
        .usingRecursiveComparison(comparisonConfig)
        .isEqualTo(CustomerPreferences.builder()
            .customerId("Ane")
            .profileName("Trotro")
            .seatPreference(NO_PREFERENCE)
            .classPreference(1)
            .language(Locale.FRENCH)
            .build());
  }

  // we could have added other tests with timeouts... (like "a real app in production", right? :>)
}
