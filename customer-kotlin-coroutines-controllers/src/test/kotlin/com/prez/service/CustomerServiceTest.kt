package com.prez.service

import com.prez.cache.CustomerCacheRepository
import com.prez.db.CustomerPreferencesRepository
import com.prez.exception.NotFoundException
import com.prez.model.Customer
import com.prez.model.CustomerPreferences
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus._019875
import com.prez.model.SeatPreference
import com.prez.model.SeatPreference.NEAR_CORRIDOR
import com.prez.model.SeatPreference.NEAR_WINDOW
import com.prez.ws.CustomerWSClient
import com.prez.ws.WebServiceException
import com.prez.ws.model.GetCustomerWSResponse
import com.prez.ws.model.Email
import com.prez.ws.model.PersonalDetails
import com.prez.ws.model.PersonalInformation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.Locale
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class CustomerServiceTest {

  private val customerCacheRepository = mock(CustomerCacheRepository::class.java)

  private val customerPreferencesRepository = mock(CustomerPreferencesRepository::class.java)

  private val customerWSClient = mock(CustomerWSClient::class.java)

  private val toTest = CustomerServiceImpl(customerWSClient, customerCacheRepository, customerPreferencesRepository)

  @BeforeEach
  fun setup() {
    reset(customerCacheRepository)
    reset(customerPreferencesRepository)
    reset(customerWSClient)
  }

  /* use runBlocking before kotlin.coroutines 1.4.0 and runBlockingTest from 1.4.0,
    see https://craigrussell.io/2019/11/unit-testing-coroutine-suspend-functions-using-testcoroutinedispatcher/ regarding issue with runBlocking */
  @Test
    fun `getCustomerInfo should return empty customer when customer does not exist`(): Unit = runBlocking {
    // Given the customer 123456789 cannot be found
    `when`(customerCacheRepository.findById("123456789")).thenReturn(Mono.empty())
    `when`(customerWSClient.getCustomer("123456789")).thenReturn(null)

    // When
    val thrown = assertFailsWith<NotFoundException> { /* behind is a runCatching{} */
      toTest.getCustomerInfo("123456789")
    }
    // Then no customer is returned
    assertThat(thrown).hasMessage("No result for the given customer id=123456789")
  }

  @Test
  fun `getCustomer should not call Customer web service when customer found in cache`(): Unit = runBlocking {
    // Given the cache does found the customer 123456789
    val expected = Customer(
      customerId = "123456789",
      firstName = "Jack",
      lastName = "Bauer",
      phoneNumber = null,
      email = "jb@boom.com",
      birthDate = LocalDate.of(1966, 2, 18),
      loyaltyProgram = LoyaltyProgram(
        number = "008",
        status = _019875,
        statusRefLabel = "_019875 IT IS",
        validityStartDate = LocalDate.now(),
        validityEndDate = LocalDate.MAX
      )
    )
    `when`(customerCacheRepository.findById("123456789")).thenReturn(
      Mono.just(expected)
    )

    // When I get the customer 123456789
    val customerInfo = toTest.getCustomerInfo("123456789")

    // Then
    assertThat(customerInfo).isEqualTo(expected)
    verify(customerCacheRepository).findById("123456789")
    verify(customerWSClient, never()).getCustomer(anyString())
  }

  @Test
  fun `getCustomerInfo should return customer from web service when customer not found in cache`(): Unit = runBlocking {
    // Given
    `when`(customerCacheRepository.findById("123456789")).thenReturn(Mono.empty())
    // The WS is called
    `when`(customerWSClient.getCustomer("123456789"))
      .thenReturn(
        GetCustomerWSResponse(
          id = "123456789",
          personalInformation = PersonalInformation(
            civility = null,
            firstName = "Jack",
            lastName = "Bower",
            alive = null,
            birthdate = null
          ),
          personalDetails = PersonalDetails(email = Email("jb@boom.com"), cell = null)
        )
      )
    `when`(customerCacheRepository.save(any())).thenReturn(Mono.just(true))

    // When
    val customer = toTest.getCustomerInfo("123456789")

    // Then
    assertThat(customer.customerId).isEqualTo("123456789")
    assertThat(customer.firstName).isEqualTo("Jack")
    assertThat(customer.lastName).isEqualTo("Bower")
    assertThat(customer.birthDate).isNull()
    assertThat(customer.phoneNumber).isNull()
    assertThat(customer.email).isEqualTo("jb@boom.com")

    verify(customerCacheRepository).findById("123456789")
    verify(customerWSClient).getCustomer("123456789")
    verify(customerCacheRepository).save(customer)
  }

  @Test
  fun `createCustomerPreferences should save with customerPreferencesRepository`(): Unit = runBlocking {
    // Given
    val expected = CustomerPreferences(
      id = "Iprefer007",
      customerId = "James",
      seatPreference = NEAR_WINDOW,
      classPreference = 1,
      profileName = "Bond",
      language = Locale.ENGLISH
    )
    val captureRequest = ArgumentCaptor.forClass(CustomerPreferences::class.java)
    `when`(customerPreferencesRepository.save(captureRequest.capture()))
      .thenReturn(Mono.just(expected))

    // When I create a customer preferences
    val customerPreferences =
      toTest.createCustomerPreferences("James", NEAR_WINDOW, 1, "Bond", Locale.ENGLISH)

    // Then
    assertThat(customerPreferences)
      .hasFieldOrPropertyWithValue("id", "Iprefer007")
      .hasFieldOrPropertyWithValue("customerId", "James")
      .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
      .hasFieldOrPropertyWithValue("classPreference", 1)
      .hasFieldOrPropertyWithValue("profileName", "Bond")
      .hasFieldOrPropertyWithValue("language", Locale.ENGLISH)
    val captured = captureRequest.value
    assertThat(captured)
      .hasFieldOrPropertyWithValue("customerId", "James")
      .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
      .hasFieldOrPropertyWithValue("classPreference", 1)
      .hasFieldOrPropertyWithValue("profileName", "Bond")
      .hasFieldOrPropertyWithValue("language", Locale.ENGLISH)
    verify(customerPreferencesRepository).save(captured)
  }

  @Test
  fun `getCustomerPreferences should return found customer preferences from repository when present`(): Unit = runBlocking {
    // Given
    val doubleZero7 = CustomerPreferences(
        id = "Iprefer007",
        customerId = "James",
        seatPreference = NEAR_WINDOW,
        classPreference = 1,
        profileName = "Bond",
        language = Locale.ENGLISH
    )
    val gordon = CustomerPreferences(
        id = "IpreferJim",
        customerId = "James",
        seatPreference = NEAR_CORRIDOR,
        classPreference = 2,
        profileName = "Gordon",
        language = Locale.ENGLISH
    )

    `when`(customerPreferencesRepository.findByCustomerId("James")).thenReturn(Flux.just(doubleZero7, gordon))

    // When
    val customerPreferences = toTest.getCustomerPreferences("James").toList()

    // Then
    assertThat(customerPreferences).isNotNull
    assertThat(customerPreferences).hasSize(2)
    assertThat(customerPreferences[0])
        .hasFieldOrPropertyWithValue("id", "Iprefer007")
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_WINDOW)
        .hasFieldOrPropertyWithValue("classPreference", 1)
        .hasFieldOrPropertyWithValue("profileName", "Bond")
        .hasFieldOrPropertyWithValue("language", Locale.ENGLISH)
    assertThat(customerPreferences[1])
        .hasFieldOrPropertyWithValue("customerId", "James")
        .hasFieldOrPropertyWithValue("seatPreference", NEAR_CORRIDOR)
        .hasFieldOrPropertyWithValue("classPreference", 2)
        .hasFieldOrPropertyWithValue("profileName", "Gordon")
        .hasFieldOrPropertyWithValue("language", Locale.ENGLISH)
    verify(customerPreferencesRepository).findByCustomerId("James")
  }

  @Test
  fun `getCustomerPreferences should return nothing when no customer preferences in repository`(): Unit = runBlocking {
    // Given
    `when`(customerPreferencesRepository.findByCustomerId("James")).thenReturn(Flux.empty())

    // When
    val thrown = assertFailsWith<NotFoundException> { /* behind is a runCatching{} */
      toTest.getCustomerPreferences("James").toList()
    }

    // Then
    assertThat(thrown).isNotNull
    assertThat(thrown).hasMessage("No result for the given customer id=James")
    verify(customerPreferencesRepository).findByCustomerId("James")
  }

  /*
   Et là ... vous vous demandez surement pourquoi ces 2 fonctions ?
   La réponse est ici : https://medium.com/@elye.project/befriending-kotlin-and-mockito-1c2e7b0ef791
   */
  private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
  }

  private fun <T> uninitialized(): T = null as T
}
