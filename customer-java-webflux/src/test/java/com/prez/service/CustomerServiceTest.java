package com.prez.service;

import static com.prez.model.LoyaltyStatus._019875;
import static com.prez.model.SeatPreference.NEAR_WINDOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prez.cache.CustomerCacheRepository;
import com.prez.exception.NotFoundException;
import com.prez.model.Customer;
import com.prez.model.CustomerPreferences;
import com.prez.model.LoyaltyProgram;
import com.prez.ws.CustomerWSClient;
import com.prez.ws.model.CreateCustomerPreferencesWSRequest;
import com.prez.ws.model.CreateCustomerPreferencesWSResponse;
import com.prez.ws.model.Email;
import com.prez.ws.model.GetCustomerWSResponse;
import com.prez.ws.model.PersonalDetails;
import com.prez.ws.model.PersonalInformation;
import java.time.LocalDate;
import java.util.Locale;
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
  @DisplayName("getCustomerInfo should throw not found exception when customer does not exist")
  void shouldReturnEmpty_whenCustomerDoesNotExists() {
    // Given the customer 123456789 cannot be found
    when(customerCacheRepository.findById("123456789")).thenReturn(Mono.empty());
    when(customerWSClient.getCustomer("123456789")).thenReturn(Mono.empty());

    // When
    final NotFoundException thrown = catchThrowableOfType(() -> toTest.getCustomerInfo("123456789").block(),
        NotFoundException.class);

    // Then no customer is returned
    assertThat(thrown).isNotNull();
    assertThat(thrown.getLocalizedMessage()).isEqualTo("No result for the given customer id=123456789");
  }

  @Test
  @DisplayName("getCustomerInfo should not call Customer web service when customer found in cache")
  void shouldReturnCachedCustomer_whenCustomerFoundInCache() {
    // Given the cache does found the customer 123456789
    Customer expected = Customer.builder()
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
        .build();
    when(customerCacheRepository.findById("123456789")).thenReturn(Mono.just(expected));

    // When I get the customer 123456789
    final Customer customer = toTest.getCustomerInfo("123456789").block();

    // Then
    assertThat(customer).isEqualTo(expected);
    verify(customerCacheRepository).findById("123456789");
    verify(customerWSClient, never()).getCustomer("123456789");
  }

  @Test
  @DisplayName("getCustomerInfo should return customer from web service when customer not found in cache")
  void shouldCallCustomerWebService_whenCustomerNotInCache() {
    //Given the repository does not found the account 123456789
    when(customerCacheRepository.findById("123456789")).thenReturn(Mono.empty());
    // The WS is called
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("123456789")
        .personalInformation(PersonalInformation.builder().firstName("Jack").lastName("Bower").build())
        .personalDetails(PersonalDetails.builder().email(Email.builder().address("jb@boom.com").build()).build())
        .build();
    when(customerWSClient.getCustomer("123456789")).thenReturn(Mono.just(getCustomerWSResponse));
    //and so is the mapper
    final Customer expected = Customer.builder()
        .customerId("123456789")
        .firstName("Jack")
        .lastName("Bower")
        .email("jb@boom.com")
        .build();
    when(mapper.toCustomer(getCustomerWSResponse)).thenReturn(expected);
    // and everything is saved
    when(customerCacheRepository.save(expected)).thenReturn(Mono.just(true));

    // Test
    final Customer customer = toTest.getCustomerInfo("123456789").block();

    // Assert
    assertThat(customer).isEqualTo(expected);
    verify(customerCacheRepository).findById("123456789");
    verify(customerWSClient).getCustomer("123456789");
    verify(customerCacheRepository).save(expected);
  }

  @Test
  @DisplayName("createCustomerPreferences should call Customer web service when Locale provided")
  void shouldCallCreateCustomerPreferences_whenLocaleProvided() {
    // Given
    final CreateCustomerPreferencesWSResponse expected = CreateCustomerPreferencesWSResponse.builder()
        .id("Iprefer007")
        .seatPreference("NEAR_WINDOW")
        .classPreference(1)
        .profileName("Bond")
        .build();
    final ArgumentCaptor<CreateCustomerPreferencesWSRequest> captureRequest =
        ArgumentCaptor.forClass(CreateCustomerPreferencesWSRequest.class);
    when(customerWSClient.createCustomerPreferences(eq("James"), captureRequest.capture(), eq(Locale.ENGLISH)))
        .thenReturn(Mono.just(expected));

    // When I create a customer preferences
    final CustomerPreferences customerPreferences =
        toTest.createCustomerPreferences("James", "NEAR_WINDOW", 1, "Bond", Locale.ENGLISH).block();

    // Then
    assertThat(customerPreferences)
        .hasFieldOrPropertyWithValue("id", "Iprefer007")
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond");

    final CreateCustomerPreferencesWSRequest captured = captureRequest.getValue();
    assertThat(captured)
        .hasFieldOrPropertyWithValue("seatPreference", "NEAR_WINDOW")
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond");

    verify(customerWSClient).createCustomerPreferences("James", captured, Locale.ENGLISH);
  }

  @Test
  @DisplayName("createCustomerPreferences should call Customer web service when no Locale")
  void shouldCallCreateCustomerPreferences_whenNoLocale() {
    // Given
    final CreateCustomerPreferencesWSResponse expected = CreateCustomerPreferencesWSResponse.builder()
        .id("Iprefer007")
        .seatPreference("NEAR_WINDOW")
        .classPreference(1)
        .profileName("Bond")
        .build();
    final ArgumentCaptor<CreateCustomerPreferencesWSRequest> captureRequest =
        ArgumentCaptor.forClass(CreateCustomerPreferencesWSRequest.class);
    when(customerWSClient.createCustomerPreferences(eq("James"), captureRequest.capture(), isNull()))
        .thenReturn(Mono.just(expected));

    // When I create a customer preferences
    final CustomerPreferences customerPreferences =
        toTest.createCustomerPreferences("James", "NEAR_WINDOW", 1, "Bond", null).block();

    // Then
    assertThat(customerPreferences)
        .hasFieldOrPropertyWithValue("id", "Iprefer007")
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond");

    final CreateCustomerPreferencesWSRequest captured = captureRequest.getValue();
    assertThat(captured)
        .hasFieldOrPropertyWithValue("seatPreference", "NEAR_WINDOW")
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond");

    verify(customerWSClient).createCustomerPreferences(eq("James"), eq(captured), isNull());
  }
}