package com.prez.it;

import static com.prez.model.SeatPreference.NEAR_WINDOW;
import static com.prez.model.SeatPreference.NO_PREFERENCE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

import com.prez.Application;
import com.prez.UsingMongoDBAndRedis;
import com.prez.api.dto.CustomerPreferencesProfileResponse;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.utils.FakeTokenGenerator;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
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
class GetCustomerPreferencesIntegrationTest extends UsingMongoDBAndRedis {
  private static final String CUSTOMERS_PREFERENCES_ENDPOINT = "/customers/preferences";

  @LocalServerPort
  private int localServerPort;
  @Autowired
  private KeyValueAdapter redisKeySpace;
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
    redisKeySpace.deleteAllOf("customer");
    mongoOperations.dropCollection("preferences");
  }

  @Test
  @DisplayName("should return 401 unauthorized when not authenticated user")
  void getCustomerPreferences_shouldReturn401_whenUserNotAuthenticated() throws Exception {
    // Given no authentication

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(null, httpHeaders, GET, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferences> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, GET, requestEntity, CustomerPreferences.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("should return 403 forbidden when authenticated user with insufficient privileges")
  void getCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() throws Exception {
    // Given
    final String insufficientPrivileges = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "not.enough");
    httpHeaders.set("Authorization", "Bearer " + insufficientPrivileges);

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(null, httpHeaders, GET, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferences> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, GET, requestEntity, CustomerPreferences.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody()).isNull();
  }

  @Test
  @DisplayName("should return not found when no existing customer preferences")
  void getCustomerPreferences_shouldReturnNoContent_whenNoPreferences() throws Exception {
    // Given
    final String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    httpHeaders.set("Authorization", "Bearer " + validToken);

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(null, httpHeaders, GET, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<String> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, GET, requestEntity, String.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(responseEntity.getBody()).isEqualTo("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given customer id=trotro\"}");
  }

  @Test
  @DisplayName("should return OK when found customer preferences")
  void getCustomerPreferences_shouldReturn200_whenFoundPreferences() throws Exception {
    // Given
    final String validToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    httpHeaders.set("Authorization", "Bearer " + validToken);

    mongoOperations.save(CustomerPreferences.builder()
        .id("L-Ane")
        .customerId("trotro")
        .profileName("rigolo")
        .seatPreference(SeatPreference.NO_PREFERENCE)
        .classPreference(2)
        .build());
    mongoOperations.save(CustomerPreferences.builder()
        .id("Ane")
        .customerId("trotro")
        .profileName("drole")
        .seatPreference(SeatPreference.NEAR_WINDOW)
        .classPreference(1)
        .build());

    // When && Then
    RequestEntity<Object> requestEntity = new RequestEntity<>(null, httpHeaders, GET, new URI(CUSTOMERS_PREFERENCES_ENDPOINT));
    ResponseEntity<CustomerPreferencesProfileResponse[]> responseEntity =
        this.restTemplate.exchange(baseUrl + CUSTOMERS_PREFERENCES_ENDPOINT, GET, requestEntity, CustomerPreferencesProfileResponse[].class);

    // When && Then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody())
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
                CustomerPreferencesProfileResponse.builder()
                    .id("L-Ane")
                    .customerId("trotro")
                    .profileName("rigolo")
                    .seatPreference(NO_PREFERENCE)
                    .classPreference(2)
                    .build(),
                CustomerPreferencesProfileResponse.builder()
                    .id("Ane")
                    .customerId("trotro")
                    .profileName("drole")
                    .seatPreference(NEAR_WINDOW)
                    .classPreference(1)
                    .build()
            );
  }
}
