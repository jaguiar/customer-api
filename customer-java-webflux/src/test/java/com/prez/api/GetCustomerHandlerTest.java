package com.prez.api;

import static com.prez.model.LoyaltyStatus.CD7F32;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest
@AutoConfigureWebTestClient
class GetCustomerHandlerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private CustomerService customerService;

  private FakeTokenGenerator fakeTokenGenerator = new FakeTokenGenerator("get.over@here");

  @Test
  @DisplayName("GET customers should return customer and http status 200 when customer found")
  void getCustomer_shouldReturn200_when_customer_found() {
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
        .railPasses(singletonList(
            RailPass.builder()
                .number("TROID")
                .type(PassType.FROM_OUTER_SPACE)
                .typeRefLabel("FROM OUTER SPACE PASS")
                .validityStartDate(LocalDate.of(2019, 12, 23))
                .validityEndDate(LocalDate.of(2999, 12, 31))
                .build()
        ))
        .build();
    when(customerService.getCustomerInfo(eq("trotro")))
        .thenReturn(Mono.just(customer));
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");


    // Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json("{" +
            "\"customerId\":\"trotro\"," +
            "\"firstName\":\"Ane\"," +
            "\"lastName\":\"Trotro\"," +
            "\"phoneNumber\":\"06-07-08-09-10\"," +
            "\"email\":\"ane.trotro@rigo.lo\"," +
            "\"loyaltyProgram\":{\"number\":\"007\",\"label\":\"CD7F32\",\"validityStartDate\":\"2000-01-01\",\"validityEndDate\":null}," +
            "\"railPasses\":[{\"number\":\"TROID\",\"label\":\"FROM OUTER SPACE PASS\",\"validityStartDate\":\"2019-12-23\",\"validityEndDate\":\"2999-12-31\"}]" +
            "}");
    verify(customerService).getCustomerInfo(eq("trotro"));
  }

  @Test
  @DisplayName("GET customers should return 401 for unauthorized user")
  void getCustomer_shouldReturnForbiddenError_whenUnauthorizedUser() {
    // Given no authentication

    //Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Accept", APPLICATION_JSON_VALUE)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized();
    verify(customerService, never()).getCustomerInfo(anyString());
  }

  @Test
  @DisplayName("GET customers should return 401 if principal is not a principal token")
  void getCustomer_shouldReturnForbiddenError_whenFakePrincipalUser() {
    //given no real token

    //Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Authorization", "Bearer Mouahahahaha")
        .header("Accept", APPLICATION_JSON_VALUE)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized();
    verify(customerService, never()).getCustomerInfo(anyString());
  }

  @Test
  @DisplayName("GET customers should return 403 forbidden when authenticated user with insufficient privileges")
  void it_shouldReturn403_whenUserAuthenticated_withInsufficientPrivileges() {
    // Given
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.none");

    //Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .header("Accept", APPLICATION_JSON_VALUE)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isForbidden();
    verify(customerService, never()).getCustomerInfo(anyString());
  }

  @Test
  @DisplayName("GET customers should return 404 if no customer info has been found")
  void it_shouldReturn404_whenCustomerNotFound() {
    //Given
    when(customerService.getCustomerInfo(eq("trotro")))
        .thenReturn(Mono.error(new NotFoundException("trotro", "Ane")));
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");

    //Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isNotFound()
        .expectBody()
        .json("{\"code\":\"NOT_FOUND\"," +
                "\"message\":\"No result for the given Ane id=trotro\"}");
    verify(customerService).getCustomerInfo(eq("trotro"));
  }

  @Test
  @DisplayName("GET customers should return 500 if something terrible happened")
  void it_shouldReturn500_whenSomethingTerribleHappens() {
    //Given
    when(customerService.getCustomerInfo(eq("trotro")))
        .thenReturn(
            Mono.error(new RuntimeException("Mouhahahaha >:)"))
        );
    final String accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read");

    //Test & Assert
    webTestClient.get()
        .uri("/customers")
        .header("Authorization", "Bearer " + accessToken)
        .accept(MediaType.APPLICATION_JSON)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody().json("{\"code\":\"UNEXPECTED_ERROR\"," +
        "\"message\":\"Something horribly wrong happened, I could tell you what but then I’d have to kill you.\"}");
    verify(customerService).getCustomerInfo(eq("trotro"));
  }
}