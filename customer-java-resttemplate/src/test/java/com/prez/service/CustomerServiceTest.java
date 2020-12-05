package com.prez.service;

import static com.prez.model.LoyaltyStatus._019875;
import static com.prez.model.SeatPreference.NEAR_WINDOW;
import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prez.cache.CustomerCacheRepository;
import com.prez.db.CustomerPreferencesRepository;
import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock
  private CustomerCacheRepository customerCacheRepository;
  @Mock
  private CustomerPreferencesRepository customerPreferencesRepository;
  @Mock
  private CustomerWSClient customerWSClient;
  @Mock
  private CustomerWSResponseToCustomerMapper mapper;

  @InjectMocks
  private CustomerService toTest;

  @BeforeEach
  void setup() {
    reset(customerCacheRepository);
    reset(customerPreferencesRepository);
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

  @Test
  @DisplayName("createCustomerPreferences should save with customerPreferencesRepository")
  void shouldCallcustomerPreferencesRepository() {
    // Given
    final CustomerPreferences expected = CustomerPreferences.builder()
        .id("Iprefer007")
        .customerId("James")
        .seatPreference(NEAR_WINDOW)
        .classPreference(1)
        .profileName("Bond")
        .language(ENGLISH)
        .build();
    final ArgumentCaptor<CustomerPreferences> captureRequest =
        ArgumentCaptor.forClass(CustomerPreferences.class);
    when(customerPreferencesRepository.save(captureRequest.capture()))
        .thenReturn(expected);

    // When I create a customer preferences
    final CustomerPreferences customerPreferences =
        toTest.createCustomerPreferences("James", NEAR_WINDOW, 1, "Bond", ENGLISH);

    // Then
    assertThat(customerPreferences)
        .hasFieldOrPropertyWithValue("id", "Iprefer007")
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond")
        .hasFieldOrPropertyWithValue("language", ENGLISH);

    final CustomerPreferences captured = captureRequest.getValue();
    assertThat(captured)
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond")
        .hasFieldOrPropertyWithValue("language", ENGLISH);

    verify(customerPreferencesRepository).save(captured);
  }
  // TODO get customer preferences

}
