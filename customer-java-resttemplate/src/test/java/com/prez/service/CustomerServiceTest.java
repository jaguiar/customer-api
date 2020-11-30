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

  // TODO create customer

    /*
    @Test
    void createAccount_shouldCalledSncfClientWithToMigrateFalse_whenCalled() throws Exception {
        //Arrange
        final String email = "youpi@yopmail.com";
        final String password = "pass;1234";
        final String firstName = "Blaise";
        final String lastname = "S";
        final LocalDate birthDate = LocalDate.of(1922, 04, 01);
        final Locale language = Locale.FRENCH;

        //Act
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        service.createAccount(email, password, firstName, lastname, birthDate, language);

        //Assert
        ArgumentCaptor<CreateCustomer> argumentCaptor = ArgumentCaptor.forClass(CreateCustomer.class);
        verify(customerWSClient).createAccount(argumentCaptor.capture(), eq(false), eq(language));
        assertThat(argumentCaptor.getValue())
                .hasFieldOrPropertyWithValue("email", email)
                .hasFieldOrPropertyWithValue("password", password)
                .hasFieldOrPropertyWithValue("firstname", firstName)
                .hasFieldOrPropertyWithValue("lastname", lastname)
                .hasFieldOrPropertyWithValue("birthdate", birthDate);
    }

    @Test(expected = NotMatchingDataException.class)
    void getAccountInfo_shouldThrowNoMatchindDataException_whenNoDevice() {
        // Given
        Customer customerWithoutDevice = Customer.builder().build();
        when(jwtValidator.getIuc(VALID_TOKEN)).thenReturn("35adcf57-2cf7-4945-a980-e9753eb146f7");
        when(accountRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7")).thenReturn(Optional.of(customerWithoutDevice));

        // When
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        service.getAccountInfo("deviceId", VALID_TOKEN);

        // Then expect no matching data exception
    }

    @Test(expected = NotMatchingDataException.class)
    void getAccountInfo_shouldThrowNoMatchindDataException_whenInactiveDevice() {
        // Given
        Device inactiveDevice = Device.builder().deviceId("deviceId").active(false).build();
        when(jwtValidator.getIuc(VALID_TOKEN)).thenReturn("35adcf57-2cf7-4945-a980-e9753eb146f7");
        when(accountRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7"))
                .thenReturn(Optional.of(Customer.builder()
                        .devices(singletonList(inactiveDevice))
                        .build()));

        // When
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        service.getAccountInfo("deviceId", VALID_TOKEN);

        // Then expect no matching data exception
    }

    @Test(expected = NotMatchingDataException.class)
    void getAccountInfo_shouldThrowNoMatchindDataException_whenNoDeviceMatchingDeviceId() {
        // Given
        Device wrongDevice = Device.builder().deviceId("otherDeviceId").active(true).build();
        when(jwtValidator.getIuc(VALID_TOKEN)).thenReturn("35adcf57-2cf7-4945-a980-e9753eb146f7");
        when(accountRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7"))
                .thenReturn(Optional.of(Customer.builder()
                        .devices(singletonList(wrongDevice))
                        .build()));

        // When
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        service.getAccountInfo("deviceId", VALID_TOKEN);

        // Then expect no matching data exception
    }

    @Test
    void getAccountInfo_shouldNotCallCustomerSource_whenDeviceFound_andValidToken_And_RecordInCache() {
        // Given
        Device matchindDevice = Device.builder().deviceId("deviceId").active(true).build();
        when(jwtValidator.getIuc(VALID_TOKEN)).thenReturn("35adcf57-2cf7-4945-a980-e9753eb146f7");
        when(accountRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7"))
                .thenReturn(Optional.of(Customer.builder()
                        .devices(singletonList(matchindDevice))
                        .build()));
        AccountInfo accountInfo = AccountInfo.builder()
                .iuc("35adcf57-2cf7-4945-a980-e9753eb146f7")
                .email("mission.impossible@connect.fr")
                .firstName("Jim")
                .lastName("Phelps").build();
        when(customerCacheRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7")).thenReturn(Optional.of(accountInfo));

        // When
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        Optional<CustomerSourceAccountInfoResponse> result = service.getAccountInfo("deviceId", VALID_TOKEN);

        // Then
        assertThat(result)
                .isNotEmpty()
                .hasValueSatisfying(
                        customerSourceAccountInfoResponse ->
                                assertThat(customerSourceAccountInfoResponse)
                                        .hasFieldOrPropertyWithValue("id", accountInfo.getIuc())
                                        .hasFieldOrPropertyWithValue("email", accountInfo.getEmail())
                                        .hasFieldOrPropertyWithValue("firstname", accountInfo.getFirstName())
                                        .hasFieldOrPropertyWithValue("lastname", accountInfo.getLastName())
                                        .hasFieldOrPropertyWithValue("birthDate", accountInfo.getBirthdate())
                                        .hasFieldOrPropertyWithValue("phoneNumber", accountInfo.getMobileNumber())
                                        .hasFieldOrPropertyWithValue("device", matchindDevice)
                );
        verify(customerWSClient, never()).getAccountInfo("35adcf57-2cf7-4945-a980-e9753eb146f7");
        verify(customerCacheRepository, never()).save(any());
    }

    @Test
    void getAccountInfo_shouldCallCustomerSource_whenDeviceFound_andValidToken_And_NothingInCache() {
        // Given
        Device matchingDevice = Device.builder().deviceId("deviceId").active(true).build();
        when(jwtValidator.getIuc(VALID_TOKEN)).thenReturn("35adcf57-2cf7-4945-a980-e9753eb146f7");
        when(accountRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7"))
                .thenReturn(Optional.of(Customer.builder()
                        .devices(singletonList(matchingDevice))
                        .build()));
        when(customerCacheRepository.findById("35adcf57-2cf7-4945-a980-e9753eb146f7")).thenReturn(Optional.empty());
        AccountInfo accountInfo = AccountInfo.builder()
                .iuc("35adcf57-2cf7-4945-a980-e9753eb146f7")
                .email("mission.impossible@connect.fr")
                .firstName("Jim")
                .lastName("Phelps").build();
        when(customerWSClient.getAccountInfo("35adcf57-2cf7-4945-a980-e9753eb146f7")).thenReturn(Optional.of(accountInfo));

        // When
        CustomerService service = new CustomerService(jwtValidator, customerWSClient, accountRepository, customerCacheRepository);
        Optional<CustomerSourceAccountInfoResponse> result = service.getAccountInfo("deviceId", VALID_TOKEN);

        // Then
        assertThat(result)
                .isNotEmpty()
                .hasValueSatisfying(
                        customerSourceAccountInfoResponse ->
                                assertThat(customerSourceAccountInfoResponse)
                                        .hasFieldOrPropertyWithValue("id", accountInfo.getIuc())
                                        .hasFieldOrPropertyWithValue("email", accountInfo.getEmail())
                                        .hasFieldOrPropertyWithValue("firstname", accountInfo.getFirstName())
                                        .hasFieldOrPropertyWithValue("lastname", accountInfo.getLastName())
                                        .hasFieldOrPropertyWithValue("birthDate", accountInfo.getBirthdate())
                                        .hasFieldOrPropertyWithValue("phoneNumber", accountInfo.getMobileNumber())
                                        .hasFieldOrPropertyWithValue("device", matchingDevice)
                );
        verify(customerWSClient).getAccountInfo("35adcf57-2cf7-4945-a980-e9753eb146f7");
        verify(customerCacheRepository).save(eq(accountInfo));
    }*/
}
