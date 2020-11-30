package com.prez.api;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prez.exception.NotFoundException;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import com.prez.service.CustomerService;
import com.prez.utils.FakeTokenGenerator;
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
class CustomerControllerGetCustomerPreferencesTest {

  private static final String CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST = "/customers/preferences";

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerService customerService;

  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");

  @Autowired
  private CustomerController toTest;

  @Test
  void controller_shouldExist() {
    assertThat(toTest).isNotNull();
  }

  @Test
  @DisplayName("GET customers preferences should return 401 unauthorized when not authenticated user")
  void getCustomerPreferences_shouldReturn401_whenUserNotAuthenticated() throws Exception {
    // Given no authentication

    // When && Then
    mockMvc.perform(get(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .accept(APPLICATION_JSON))
        .andExpect(status().is(401))
        .andExpect(content().string(""));
    verify(customerService, never()).getCustomerPreferences(anyString());
  }

  @Test
  @DisplayName("GET customers preferences should return 403 forbidden when authenticated user with insufficient privileges")
  void getCustomerPreferences_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "not.enough");

    // When && Then
    mockMvc.perform(get(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON))
        .andExpect(status().is(403))
        .andExpect(content().string(""));
    verify(customerService, never()).getCustomerPreferences(anyString());
  }

  @Test
  @DisplayName("GET customers preferences should return not found when no preferences")
  void getCustomerPreferences_shouldReturnNoContent_whenNoPreferences() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    when(customerService.getCustomerPreferences("trotro"))
        .thenThrow(new NotFoundException("trotro", "customer"));

    // When && Then
    mockMvc.perform(get(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON))
        .andExpect(status().is(404))
        .andExpect(content().json("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given customer id=trotro\"}"));
  }

  @Test
  @DisplayName("GET customers preferences should return OK when found preferences")
  void getCustomerPreferences_shouldReturn200_whenFoundPreferences() throws Exception {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");
    when(customerService.getCustomerPreferences("trotro"))
        .thenReturn(singletonList(CustomerPreferences.builder()
            .customerId("trotro")
            .profileName("rigolo")
            .seatPreference(SeatPreference.NO_PREFERENCE)
            .classPreference(2)
            .build()));

    // When && Then
    mockMvc.perform(get(CUSTOMERS_PREFERENCES_ENDPOINT_TO_TEST)
        .header("Authorization", "Bearer " + accessToken)
        .accept(APPLICATION_JSON))
        .andExpect(status().is(200))
        .andExpect(content().json("{\"profiles\":[{\"customerId\":\"trotro\","
            + "\"seatPreference\":\"NO_PREFERENCE\","
            + "\"classPreference\":2,"
            + "\"profileName\":\"rigolo\"}]}"));
    verify(customerService).getCustomerPreferences("trotro");
  }
}
