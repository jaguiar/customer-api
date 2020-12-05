package com.prez.it;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.prez.UsingMongoDBAndRedis;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.utils.FakeTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
class GetCustomerPreferencesIntegrationTest extends UsingMongoDBAndRedis {

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
    mongoOperations.dropCollection(CustomerPreferences.class).block();
  }

  @Test
  @DisplayName("should return 401 unauthorized when not authenticated user")
  void getCustomerPreferences_shouldReturn401_whenUserNotAuthenticated() {
    // Given no authentication

    // When && Then
    client.get().uri("/customers/preferences")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized()
        .expectBody()
        .isEmpty();
  }

  @Test
  @DisplayName("should return 403 forbidden when authenticated user with insufficient privileges")
  void getCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() {
    // Given
    final String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("trotro", 3600, "not.enough");

    // When && Then
    client.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden()
        .expectBody().json("");
  }

  @Test
  @DisplayName("should return not found when no existing customer preferences")
  void getCustomerPreferences_shouldReturnNotFound_whenNoPreferences() {
    // Given
    final String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("trotro", 3600, "customer.read");

    // When && Then
    client.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody().json("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given customer id=trotro\"}");
  }

  @Test
  @DisplayName("should return OK when found customer preferences")
  void getCustomerPreferences_shouldReturn200_whenFoundPreferences() {
    // Given
    final String accessToken = FAKE_TOKEN_GENERATOR.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    mongoOperations.save(CustomerPreferences.builder()
        .customerId("trotro")
        .profileName("rigolo")
        .seatPreference(SeatPreference.NO_PREFERENCE)
        .classPreference(2)
        .build())
        .block();
    mongoOperations.save(CustomerPreferences.builder()
        .customerId("trotro")
        .profileName("drole")
        .seatPreference(SeatPreference.NEAR_WINDOW)
        .classPreference(1)
        .build())
        .block();

    // When && Then
    client.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody().json("["
          + "{\"customerId\":\"trotro\","
          + "\"profileName\":\"rigolo\","
          + "\"seatPreference\":\"NO_PREFERENCE\","
          + "\"classPreference\":2},"
          + "{\"customerId\":\"trotro\","
          + "\"profileName\":\"drole\","
          + "\"seatPreference\":\"NEAR_WINDOW\","
          + "\"classPreference\":1}"
          + "]");
  }
}
