package com.prez.api;

import static com.prez.model.LoyaltyStatus.CD7F32;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.PassType;
import com.prez.model.RailPass;
import com.prez.service.CustomerService;
import com.prez.utils.FakeTokenGenerator;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class CustomerControllerGetCustomerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private CustomerService customerService;

  @Autowired
  private CustomerController toTest;

  private final FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("test-authorization-server");

  @Test
  void controller_shouldExist() {
    assertThat(toTest).isNotNull();
  }


  @Test
  @DisplayName("GET customers should return customer and http status 200 when customer found")
  void getCustomer_shouldReturn200_when_customer_found() throws Exception {
    // Given
    final Customer customer = Customer.builder()
        .customerId("trotro")
        .firstName("Ane")
        .lastName("Trotro")
        .email("ane.trotro@rigo.lo")
        .phoneNumber("06-07-08-09-10")
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("007")
            .status(CD7F32)
            .statusRefLabel(null)
            .validityStartDate(LocalDate.of(2000, 1, 1))
            .validityEndDate(null)
            .build())
        .railPasses(singletonList(RailPass.builder()
            .number("TROID")
            .type(PassType.FROM_OUTER_SPACE)
            .typeRefLabel("FROM OUTER SPACE PASS")
            .validityStartDate(LocalDate.of(2019, 12, 23))
            .validityEndDate(LocalDate.of(2999, 12, 31))
            .build()))
        .build();
    when(customerService.getCustomerInfo("trotro"))
        .thenReturn(customer);
    String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read customer.write");


    // When && Then
    this.mockMvc.perform(get("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .header("Content-Type", APPLICATION_JSON_VALUE))
        .andExpect(status().is(200))
        .andExpect(content().json(
            "{\"customerId\":\"trotro\",\"firstName\":\"Ane\",\"lastName\":\"Trotro\"," +
                    "\"phoneNumber\":\"06-07-08-09-10\",\"email\":\"ane.trotro@rigo.lo\"," +
                    "\"loyaltyProgram\":{\"number\":\"007\",\"label\":\"CD7F32\"," +
                    "\"validityStartDate\":\"2000-01-01\"},\"railPasses\":[{\"number\":\"TROID\",\"label\":\"FROM OUTER SPACE PASS\"," +
                    "\"validityStartDate\":\"2019-12-23\",\"validityEndDate\":\"2999-12-31\"}]}",
            true));

  }

  @Test
  @DisplayName("GET customers should return 401 for unauthorized user")
  void getCustomer_shouldReturnForbiddenError_whenUnauthorizedUser() throws Exception {
    this.mockMvc.perform(get("/customers")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is(401));
  }

  @Test
  @DisplayName("GET customers should return 404 when customer not found")
  void getCustomer_shouldReturn404_whenCustomerNotFound() throws Exception {
    // Given
    when(customerService.getCustomerInfo("nobody"))
        .thenThrow(new NotFoundException("nobody", "customer"));
    String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("nobody", 3600, "customer.read customer.write");

    // When && Then
    this.mockMvc.perform(get("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(404))
        .andExpect(content().json("{\"code\":\"NOT_FOUND\",\"message\":\"No result for the given customer id=nobody\"}"));
  }


}