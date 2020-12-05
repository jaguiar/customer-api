package com.prez.api;

import static com.prez.model.SeatPreference.NO_PREFERENCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.service.CustomerService;
import com.prez.utils.FakeTokenGenerator;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles({"test"})
class CustomerControllerCreateCustomerPreferencesTest {

  private static final String CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST = "/customers/preferences";
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerService customerService;


  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");

  @Test
  @DisplayName("POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid()
      throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write");

    when(customerService.createCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), eq(Locale.FRENCH)))
        .thenReturn(CustomerPreferences.builder()
            .id("ane.trotro@rigo.lo")
            .customerId("Ane")
            .seatPreference(NO_PREFERENCE)
            .classPreference(1)
            .profileName("Trotro")
            .build());

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NO_PREFERENCE\","
            + "\"classPreference\":1,"
            + "\"profileName\":\"Trotro\","
            + "\"language\":\"fr\""
            + "}"))
        .andExpect(status().is(201))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"id\":\"ane.trotro@rigo.lo\"," +
            "\"customerId\":\"Ane\"," +
            "\"seatPreference\":\"NO_PREFERENCE\"," +
            "\"classPreference\":1," +
            "\"profileName\":\"Trotro\"}"));
    verify(customerService)
        .createCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), eq(Locale.FRENCH));
  }

  @Test
  @DisplayName("POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input and null language")
  void createCustomerPreferences_shouldReturnOK_whenCustomerPreferencesSuccessfullyCreatedWithUserAuthenticated_andInputValid_andNullLanguage()
      throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write");

    when(customerService.createCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), isNull()))
        .thenReturn(CustomerPreferences.builder()
            .id("ane.trotro@rigo.lo")
            .customerId("Ane")
            .seatPreference(NO_PREFERENCE)
            .classPreference(1)
            .profileName("Trotro")
            .build());

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NO_PREFERENCE\","
            + "\"classPreference\":1,"
            + "\"profileName\":\"Trotro\""
            + "}"))
        .andExpect(status().is(201))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"id\":\"ane.trotro@rigo.lo\"," +
            "\"customerId\":\"Ane\"," +
            "\"seatPreference\":\"NO_PREFERENCE\"," +
            "\"classPreference\":1," +
            "\"profileName\":\"Trotro\"}"));
    verify(customerService)
        .createCustomerPreferences(eq("Ane"), eq(NO_PREFERENCE), eq(1), eq("Trotro"), isNull());
  }

  @Test
  @DisplayName("POST customers should return 400 bad request when language is not valid")
  void createCustomer_shouldReturn400_whenLanguageNotValid() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_WINDOW\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"PasAssezPasAssez\","
            + "\"language\":\"bleh\""
            + "}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The language is not valid. Accepted languages are : fr,de,es,en,it,pt]\"}"));
    verify(customerService, never()).createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 403 forbidden when not authenticated user")
  void createCustomerPreferences_shouldReturn403_whenUserNotAuthenticated() throws Exception {
    // Given no authentication

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_WINDOW\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"PasAssezPasAssez\""
            + "}"))
        .andExpect(status().is(403))
        .andExpect(content().string(""));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 403 forbidden when authenticated user with insufficient privileges")
  void createCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_CORRIDOR\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"PasAssezPasAssez\""
            + "}"))
        .andExpect(status().is(403))
        .andExpect(content().string(""));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when seat preference is null")
  void createCustomerPreferences_shouldReturn400_whenSeatPreferenceNull() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"classPreference\":2,"
            + "\"profileName\":\"PasAssezPasAssez\","
            + "\"language\":\"it\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The seat preference is missing]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when seat preference is null")
  void createCustomerPreferences_shouldReturn400_whenClassPreferenceNull() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_CORRIDOR\","
            + "\"profileName\":\"PasAssezPasAssez\","
            + "\"language\":\"de\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The class preference is missing]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when classPreference is not valid")
  void createCustomerPreferences_shouldReturn400_whenClassPreferenceNotValid() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"classPreference\":3,"
            + "\"seatPreference\":\"NEAR_CORRIDOR\","
            + "\"profileName\":\"PasAssezPasAssez\","
            + "\"language\":\"en\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [Max value for class preference is 2]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is null")
  void createCustomerPreferences_shouldReturn400_whenProfileNameNull() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_CORRIDOR\","
            + "\"classPreference\":2,"
            + "\"language\":\"fr\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The profile name is missing]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName does not respect pattern")
  void createCustomerPreferences_shouldReturn400_whenProfileNameDoesNotRespectPattern() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_WINDOW\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"???!PasAssezPasAssez???\","
            + "\"language\":\"fr\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The profile name contains forbidden characters]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }

  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is empty")
  void createCustomerPreferences_shouldReturn400_whenProfileNameIsEmpty() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_WINDOW\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"\","
            + "\"language\":\"fr\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The profile name should have a size between 1 and 50 characters]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }


  @Test
  @DisplayName("POST customers preferences should return 400 bad request when profileName is too long")
  void createCustomerPreferences_shouldReturn400_whenProfileNameIsTooLong() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write");

    // When && Then
    mockMvc.perform(post(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON)
        .contentType(APPLICATION_JSON)
        .content("{"
            + "\"seatPreference\":\"NEAR_WINDOW\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"blablablablablablablablablbalbalbalbalblablablablablablablablablablablablablablablbalbalbalbalbalbalbalablablbalbalbalbalbalbalbalbalbalbalbalbalbalbalblabalbalbalbablablablablablablabla\","
            + "\"language\":\"fr\""
            +"}"))
        .andExpect(status().is(400))
        .andExpect(content().contentType(APPLICATION_JSON))
        .andExpect(content().json("{\"code\":\"VALIDATION_ERROR\",\"message\":\"1 error(s) while validating createCustomerPreferencesRequest : [The profile name should have a size between 1 and 50 characters]\"}"));
    verify(customerService, never())
        .createCustomerPreferences(anyString(), any(SeatPreference.class), anyInt(), anyString(), any(Locale.class));
  }
}