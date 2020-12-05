package com.prez.api;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.prez.exception.NotFoundException;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.service.CustomerService;
import com.prez.utils.FakeTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@SpringBootTest
@AutoConfigureWebTestClient
class GetCustomerPreferencesHandlerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private CustomerService customerService;

  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");

  @Test
  @DisplayName("GET customers preferences should return 401 unauthorized when not authenticated user")
  void getCustomerPreferences_shouldReturn401_whenUserNotAuthenticated() {
    // Given no authentication

    // When && Then
    webTestClient.get().uri("/customers/preferences")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized()
        .expectBody()
        .isEmpty();
    verify(customerService, never()).getCustomerPreferences(anyString());
  }

  @Test
  @DisplayName("GET customers preferences should return 403 forbidden when authenticated user with insufficient privileges")
  void getCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "not.enough");

    // When && Then
    webTestClient.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden()
        .expectBody().json("");
    verify(customerService, never()).getCustomerPreferences(anyString());
  }

  @Test
  @DisplayName("GET customers preferences should return not found when no preferences")
  void getCustomerPreferences_shouldReturnNotFound_whenNoPreferences() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    when(customerService.getCustomerPreferences("trotro"))
        .thenReturn(Flux.empty());

    // When && Then
    webTestClient.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody().json("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given Ane id=trotro\"}");
  }

  @Test
  @DisplayName("GET customers preferences should return OK when found preferences")
  void getCustomerPreferences_shouldReturn200_whenFoundPreferences() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    when(customerService.getCustomerPreferences("trotro"))
        .thenReturn(Flux.just(CustomerPreferences.builder()
            .customerId("trotro")
            .profileName("rigolo")
            .seatPreference(SeatPreference.NO_PREFERENCE)
            .classPreference(2)
            .build()));

    // When && Then
    webTestClient.get().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody().json("[{\"customerId\":\"trotro\","
        + "\"seatPreference\":\"NO_PREFERENCE\","
        + "\"classPreference\":2,"
        + "\"profileName\":\"rigolo\"}]");
    verify(customerService).getCustomerPreferences("trotro");
  }
}