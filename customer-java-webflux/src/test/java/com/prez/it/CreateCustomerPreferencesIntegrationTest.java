package com.prez.it;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.prez.UsingMongoDBAndRedis;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.utils.FakeTokenGenerator;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("docker")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
class CreateCustomerPreferencesIntegrationTest extends UsingMongoDBAndRedis {

  private static final FakeTokenGenerator FAKE_TOKEN_GENERATOR = new FakeTokenGenerator("test-authorization-server");

  @Autowired
  private WebTestClient client;
  @Autowired
  private ReactiveRedisTemplate<String, Customer> customerInfoRedisTemplate;
  @Autowired
  private ReactiveMongoOperations mongoOperations;

  @BeforeEach
  void beforeEach() {
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block();
    mongoOperations.dropCollection(CustomerPreferences.class);
  }

  @Test
  @DisplayName("should return Bad Request when missing mandatory fields")
  void shouldReturnBadRequest_whenMissingMandatoryFields() throws URISyntaxException {
    // Given
    String validToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("cached", 3600, "customer.write");

    // When
    String toCreate = "{"
        + "\"seatPreference\": \"NEAR_WINDOW\""
        + "}";

    // Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + validToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .bodyValue(toCreate)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
        .jsonPath("$.message")
        .value(startsWith("2 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name is missing"))
        .jsonPath("$.message").value(containsString("The class preference is missing"));
  }

  @Test
  @DisplayName("should return Bad Request when invalid input values")
  void shouldReturnBadRequest_whenInvalidInputValues() throws URISyntaxException {
    // Given
    String validToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("cached", 3600, "customer.write");

    // When
    String toCreate = "{"
        + "\"seatPreference\":\"NEAR_CORRIDOR\","
        + "\"classPreference\":23,"
        + "\"profileName\":\"Trotro*\","
        + "\"language\":\"meh\""
        + "}";

    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + validToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .bodyValue(toCreate)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
        .jsonPath("$.message")
        .value(startsWith("3 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name contains forbidden characters"))
        .jsonPath("$.message").value(containsString("Max value for class preference is 2"))
        .jsonPath("$.message")
        .value(containsString("The language is not valid. Accepted languages are : fr,de,es,en,it,pt"));
    // Then
  }

  @Test
  @DisplayName("should return Bad Request when malformed request")
  void shoud_return_bad_request_when_malformed_request() {
    // Given
    String validToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("cached", 3600, "customer.write");

    // When
    String toCreate = "{\"seatPreference\":\"NEAR_WINDOW\",\"}";

    // Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + validToken)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(toCreate)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"Bad input\"}");
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
    client
        .post()
        .uri("/customers/preferences")
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .bodyValue(validRequest)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  @DisplayName("should return 403 forbidden when authenticated user with insufficient privileges")
  void createCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() throws Exception {
    // Given
    String insufficientPrivileges = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("cached", 3600, "customer.read");
    String validRequest = "{"
        + "\"seatPreference\":\"NEAR_WINDOW\","
        + "\"classPreference\":2,"
        + "\"profileName\":\"PasAssezPasAssez\","
        + "\"language\":\"fr\""
        + "}";

    // When && Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + insufficientPrivileges)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .bodyValue(validRequest)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden();
  }

  @Test
  @DisplayName("should return created preferences when customer preferences successfully created for authorized user with valid input")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid() {
    // Given
    final String validToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("Ane", 3600, "customer.write");
    String validRequest = "{"
        + "\"seatPreference\":\"NO_PREFERENCE\","
        + "\"classPreference\":1,"
        + "\"profileName\":\"Trotro\","
        + "\"language\":\"fr\""
        + "}";

    // When && Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + validToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .bodyValue(validRequest)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isCreated()
        .expectBody().json("{"
        + "\"seatPreference\":\"NO_PREFERENCE\","
        + "\"classPreference\":1,"
        + "\"profileName\":\"Trotro\","
        + "\"language\":\"fr\""
        + "}"
    );
  }

  // we could have added other tests with timeouts... (like "a real app in production", right? :>)
}
