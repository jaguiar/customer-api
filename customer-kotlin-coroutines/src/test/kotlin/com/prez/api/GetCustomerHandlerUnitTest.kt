package com.prez.api

import com.prez.api.dto.CustomerResponse
import com.prez.api.dto.LoyaltyProgramResponse
import com.prez.api.dto.RailPassResponse
import com.prez.exception.NotFoundException
import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus
import com.prez.model.PassType
import com.prez.model.RailPass
import com.prez.service.CustomerService
import com.prez.utils.FakeTokenGenerator
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.server.EntityResponse
import java.time.LocalDate
import java.time.Period.between
import kotlin.test.assertFailsWith

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@ExtendWith(SpringExtension::class)
internal class GetCustomerHandlerUnitTest {

  private val mockCustomerService = mock(CustomerService::class.java)
  private val toTest = GetCustomerHandler(mockCustomerService)

  private val fakeTokenGenerator: FakeTokenGenerator = FakeTokenGenerator("get.over@here")

  @BeforeEach
  internal fun beforeEach() {
    reset(mockCustomerService)
  }

  /* use runBlocking before kotlin.coroutines 1.4.0 and runBlockingTest from 1.4.0,
    see https://craigrussell.io/2019/11/unit-testing-coroutine-suspend-functions-using-testcoroutinedispatcher/ regarding issue with runBlocking
    */
  @Test
  fun `getCustomer should return customer and http status 200 when customer found`(): Unit = runBlocking {
    //given
    val request = MockServerRequest.builder()
      .principal(validToken("neo"))
      .build()
    `when`(mockCustomerService.getCustomerInfo(eq("neo"))).thenReturn(
      Customer(
        customerId = "neo",
        email = "neo@matrix.com",
        firstName = "Thomas A.",
        lastName = "Anderson",
        birthDate = LocalDate.of(1971, 9, 13),
        phoneNumber = "0102030405",
        loyaltyProgram = LoyaltyProgram(
          number = "29090109123456789",
          statusRefLabel = null,
          status = LoyaltyStatus.B0B0B0,
          validityStartDate = LocalDate.of(2012, 7, 1),
          validityEndDate = LocalDate.of(2013, 7, 2)
        ),
        railPasses = listOf(
          RailPass(
            number = "652140102420412755",
            type = PassType.FAMILY,
            typeRefLabel = null,
            validityStartDate = LocalDate.of(2020, 4, 18),
            validityEndDate = LocalDate.of(2020, 3, 14)
          )
        )
      )
    )

    //Test
    val actual = toTest.getCustomer(request)

    //assert
    assertThat(actual)
      .hasFieldOrPropertyWithValue("statusCode", HttpStatus.OK.value())
    assertThat((actual as EntityResponse<*>).entity()).usingRecursiveComparison().isEqualTo(
      CustomerResponse(
        customerId = "neo",
        firstName = "Thomas A.",
        lastName = "Anderson",
        age = between(LocalDate.of(1971, 9, 13), LocalDate.now()).years,
        email = "neo@matrix.com",
        phoneNumber = "0102030405",
        loyaltyProgram = LoyaltyProgramResponse(
          number = "29090109123456789",
          label = "B0B0B0",
          validityStartDate = LocalDate.parse("2012-07-01"),
          validityEndDate = LocalDate.parse("2013-07-02")
        ),
        railPasses = listOf(
          RailPassResponse(
            number = "652140102420412755",
            label = "FAMILY",
            validityStartDate = LocalDate.parse("2020-04-18"),
            validityEndDate = LocalDate.parse("2020-03-14")
          )
        )
      )
    )
    verify(mockCustomerService).getCustomerInfo(eq("neo"))
  }

  @Test
  fun `getCustomer should return customer when neither loyalty nor demat pass are present`(): Unit = runBlocking {
    //given
    val request = MockServerRequest.builder()
      .principal(validToken("neo"))
      .build()
    `when`(mockCustomerService.getCustomerInfo(eq("neo"))).thenReturn(
      Customer(
        customerId = "aliveCustomerId",
        birthDate = null,
        lastName = "Le Quesnoy",
        firstName = "Bernadette",
        email = "mail@mail.com",
        phoneNumber = "06-07-08-09-10",
        railPasses = listOf()
      )
    )

    //Test
    val actual = toTest.getCustomer(request)

    //assert
    assertThat(actual)
      .hasFieldOrPropertyWithValue("statusCode", HttpStatus.OK.value())
    assertThat((actual as EntityResponse<*>).entity()).usingRecursiveComparison()
      .ignoringExpectedNullFields().isEqualTo(
        CustomerResponse(
          customerId = "aliveCustomerId",
          age = null,
          lastName = "Le Quesnoy",
          firstName = "Bernadette",
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf()
        )
      )
    verify(mockCustomerService).getCustomerInfo(eq("neo"))
  }

  @Test
  fun `getCustomer should throw not found exception if no customer info has been found`(): Unit = runBlocking {
    //given
    val request = MockServerRequest.builder()
      //.principal(PrincipalToken("customerId", Device("1234", LocalDateTime.now(), true), "123456789"))
      .principal(validToken("nonexistent"))
      .build()
    `when`(mockCustomerService.getCustomerInfo(eq("nonexistent"))).thenThrow(NotFoundException("nonexistent", "being"))

    //Test
    val thrown = assertFailsWith<NotFoundException> {
      toTest.getCustomer(request)
    }

    //assert
    assertThat(thrown.localizedMessage).isEqualTo("No result for the given being id=nonexistent")

    verify(mockCustomerService).getCustomerInfo("nonexistent")
  }

  @Test
  fun `getCustomer should return 500 if something terrible happened`(): Unit = runBlocking {
    //given
    val request = MockServerRequest.builder()
      .principal(validToken("customerId"))
      .build()
    `when`(mockCustomerService.getCustomerInfo(eq("customerId"))).thenThrow(RuntimeException("Mouahahaha >:)"))

    //Test
    val thrown = assertFailsWith<RuntimeException> { /* behind is a runCatching{} */
      toTest.getCustomer(request)
    }

    //assert
    assertThat(thrown)
      .hasMessage("Mouahahaha >:)")
    verify(mockCustomerService).getCustomerInfo("customerId")
  }

  private fun validToken(sub: String): JwtAuthenticationToken = JwtAuthenticationToken(
    Jwt
      .withTokenValue(fakeTokenGenerator.generateNotExpiredSignedToken(sub, 3600, "customer.read"))
      .header("user-header", "random")
      .subject(sub)
      //.issuedAt(Instant.now())
      //.issuer("/test-authorization-server")
      .build()
  )

  /*
   Et là ... vous vous demandez surement pourquoi ces 2 fonctions ?
   La réponse est ici : https://medium.com/@elye.project/befriending-kotlin-and-mockito-1c2e7b0ef791
   */
  private fun <T> eq(v: T): T {
    Mockito.eq(v)
    return uninitialized()
  }

  private fun <T> uninitialized(): T = null as T
}