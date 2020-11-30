package com.prez.api;

import static com.prez.model.SeatPreference.NEAR_CORRIDOR;
import static com.prez.model.SeatPreference.NEAR_WINDOW;
import static com.prez.model.SeatPreference.NO_PREFERENCE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import com.prez.api.dto.CreateCustomerPreferencesRequest;
import com.prez.model.CustomerPreferences;
import com.prez.service.CustomerService;
import com.prez.utils.FakeTokenGenerator;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
class CreateCustomerPreferencesHandlerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private CustomerService customerService;

  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");

  @Test
  @DisplayName("POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid()
      {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NO_PREFERENCE)
        .classPreference(1)
        .profileName("Trotro")
        .language("fr")
        .build();


    when(customerService.saveCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), eq(Locale.FRENCH)))
        .thenReturn(Mono.just(CustomerPreferences.builder()
            .id("ane.trotro@rigo.lo")
            .customerId("Ane")
            .seatPreference(NO_PREFERENCE)
            .classPreference(1)
            .profileName("Trotro")
            .build()));

    // When && Then
    webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("{\"id\":\"ane.trotro@rigo.lo\"," +
            "\"customerId\":\"Ane\"," +
            "\"seatPreference\":\"NO_PREFERENCE\"," +
            "\"classPreference\":1," +
            "\"profileName\":\"Trotro\"}");
    verify(customerService)
        .saveCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), eq(Locale.FRENCH));
  }

  @Test
  @DisplayName("POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input and null language")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid_andNullLanguage()
      {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NO_PREFERENCE)
        .classPreference(1)
        .profileName("Trotro")
        .build();

    when(customerService.saveCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), isNull()))
        .thenReturn(Mono.just(CustomerPreferences.builder()
            .id("ane.trotro@rigo.lo")
            .customerId("Ane")
            .seatPreference(NO_PREFERENCE)
            .classPreference(1)
            .profileName("Trotro")
            .build()));

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isCreated()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .json("{\"id\":\"ane.trotro@rigo.lo\"," +
            "\"customerId\":\"Ane\"," +
            "\"seatPreference\":\"NO_PREFERENCE\"," +
            "\"classPreference\":1," +
            "\"profileName\":\"Trotro\"}");
    verify(customerService)
        .saveCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), isNull());
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when language is not valid")
  void createCustomer_shouldReturn400_whenLanguageNotValid() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(2)
        .profileName("PasAssezPasAssez")
        .language("bleh")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The language is not valid. Accepted languages are : fr,de,es,en,it,pt"));
    verify(customerService, never()).saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 403 forbidden when not authenticated user")
  void createCustomerPreferences_shouldReturn403_whenUserNotAuthenticated() {
    // Given no authentication
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(2)
        .profileName("PasAssezPasAssez")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isForbidden()
        .expectBody(String.class)
        .isEqualTo("CSRF Token has been associated to this client");
    verify(customerService, never())
        .createCustomerPreferences(anyString(), anyString(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 403 forbidden when authenticated user with insufficient privileges")
  void createCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("PasAssezPasAssez")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isForbidden()
        .expectBody().json("");
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }


  @Test
  @DisplayName("POST customers preferences should return 400 bad request when seat preference is null")
  void createCustomerPreferences_shouldReturn400_whenSeatPreferenceNull() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .classPreference(2)
        .profileName("PasAssezPasAssez")
        .language("it")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The seat preference is missing"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when classPreference is not valid")
  void createCustomerPreferences_shouldReturn400_whenClassPreferenceNull() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .profileName("PasAssezPasAssez")
        .language("de")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The class preference is missing"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when classPreference is not valid")
  void createCustomerPreferences_shouldReturn400_whenClassPreferenceNotValid() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(3)
        .profileName("PasAssezPasAssez")
        .language("en")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("Max value for class preference is 2"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is null")
  void createCustomerPreferences_shouldReturn400_whenProfileNameNull() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .language("fr")
        .build();

    // When && Then
    webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name is missing"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName does not respect pattern")
  void createCustomerPreferences_shouldReturn400_whenProfileNameDoesNotRespectPattern() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(2)
        .profileName("???!PasAssezPasAssez???")
        .language("fr")
        .build();

    // When && Then
    webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name contains forbidden characters"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is empty")
  void createCustomerPreferences_shouldReturn400_whenProfileNameIsEmpty() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(2)
        .profileName("")
        .language("fr")
        .build();

    // When && Then
    webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name is missing"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is too long")
  void createCustomerPreferences_shouldReturn400_whenProfileNameIsTooLong() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");
    final CreateCustomerPreferencesRequest toCreate = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(2)
        .profileName(
            "blablablablablablablablablbalbalbalbalblablablablablablablablablablablablablablablbalbalbalbalbalbalbalablablbalbalbalbalbalbalbalbalbalbalbalbalbalbalblabalbalbalbablablablablablablabla")
        .language("fr")
        .build();

    // When && Then
    webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .body(fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest()
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
        .jsonPath("$.message").value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name should have a size between 1 and 50 characters"));
    verify(customerService, never())
        .saveCustomerPreferences(anyString(), any(), anyInt(), anyString(), any(Locale.class));
  }

}