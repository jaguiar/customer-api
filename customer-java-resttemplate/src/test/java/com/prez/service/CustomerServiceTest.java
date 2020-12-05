package com.prez.service;

import static com.prez.model.LoyaltyStatus._019875;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prez.cache.CustomerCacheRepository;
import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.ws.CustomerWSClient;
import com.prez.ws.model.Email;
import com.prez.ws.model.GetCustomerWSResponse;
import com.prez.ws.model.PersonalDetails;
import com.prez.ws.model.PersonalInformation;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock
  private CustomerCacheRepository customerCacheRepository;
  @Mock
  private CustomerWSClient customerWSClient;
  @Mock
  private CustomerWSResponseToCustomerMapper mapper;

  @InjectMocks
  private CustomerService toTest;

  @BeforeEach
  void setup() {
    reset(customerCacheRepository);
    reset(customerWSClient);
  }

  @Test
  @DisplayName("getCustomerInfo should throw NotFoundException when customer does not exist")
  void shouldThrowCustomerNotFoundException_whenCustomerDoesNotExists() {
    // Given the customer 123456789 cannot be found

    // When
    Throwable thrown = catchThrowable(() -> toTest.getCustomerInfo("123456789"));

    // Then a NotFoundException is thrown
    assertThat(thrown).isNotNull();
    assertThat(thrown).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("getCustomerInfo should not call Customer web service when customer found in cache")
  void shouldReturnCachedCustomer_whenCustomerFoundInCache() {
    // Given the cache does found the customer 123456789
    when(customerCacheRepository.findById("123456789")).thenReturn(Optional.of(Customer.builder()
        .customerId("123456789")
        .firstName("Jack")
        .lastName("Bauer")
        .email("jb@boom.com")
        .birthDate(LocalDate.of(1966, 2, 18))
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(_019875)
            .statusRefLabel("_019875 IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .build()));

    // When I get the customer 123456789
    Customer customer = toTest.getCustomerInfo("123456789");

    // Then
    assertThat(customer).isEqualTo(Customer.builder()
        .customerId("123456789")
        .firstName("Jack")
        .lastName("Bauer")
        .email("jb@boom.com")
        .birthDate(LocalDate.of(1966, 2, 18))
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(_019875)
            .statusRefLabel("_019875 IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .build());
    verify(customerWSClient, never()).getCustomer("123456789");
  }

  @Test
  @DisplayName("getCustomerInfo should return customer from web service when customer not found in cache")
  void shouldCallCustomerWebService_whenCustomerNotInCache() {
    //Given the repository does not found the account 123456789
    when(customerCacheRepository.findById("123456789")).thenReturn(Optional.empty());
    // The WS is called
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("123456789")
        .personalInformation(PersonalInformation.builder().firstName("Jack").lastName("Bower").build())
        .personalDetails(PersonalDetails.builder().email(Email.builder().address("jb@boom.com").build()).build())
        .build();
    when(customerWSClient.getCustomer("123456789")).thenReturn(Optional.of(getCustomerWSResponse));
    //and so is the mapper
    Customer expected = Customer.builder()
        .customerId("123456789")
        .firstName("Jack")
        .lastName("Bower")
        .email("jb@boom.com")
        .build();
    when(mapper.toCustomer(getCustomerWSResponse)).thenReturn(expected);
    // and everything is saved
    when(customerCacheRepository.save(expected)).thenReturn(expected);

    // Test
    Customer customer = toTest.getCustomerInfo("123456789");

    // Assert
    assertThat(customer).isEqualTo(expected);
    verify(customerCacheRepository).save(expected);
  }

  // TODO create customer preferences

}
