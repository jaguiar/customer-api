package com.prez.api

import com.prez.exception.NotFoundException
import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus.CD7F32
import com.prez.model.PassType
import com.prez.model.RailPass
import com.prez.service.CustomerService
import com.prez.utils.FakeTokenGenerator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureWebTestClient
internal class GetCustomerHandlerIntegrationTest(
    @Autowired val webTestClient: WebTestClient
) {

    @MockBean
    private lateinit var customerService: CustomerService

    private val fakeTokenGenerator: FakeTokenGenerator = FakeTokenGenerator("get.over@here")

    @Test
    fun `GET customers should return customer and http status 200 when customer found`(): Unit = runBlocking {
        // Given
        val customer = Customer(
            customerId = "trotro",
            firstName = "Ane",
            lastName = "Trotro",
            email = "ane.trotro@rigo.lo",
            birthDate = null,
            phoneNumber = "06-07-08-09-10",
            loyaltyProgram = LoyaltyProgram(
                number = "007",
                status = CD7F32,
                statusRefLabel = null,
                validityStartDate = LocalDate.of(2000, 1, 1),
                validityEndDate = null
            ),
            railPasses = listOf(
                RailPass(
                    number = "TROID",
                    type = PassType.FROM_OUTER_SPACE,
                    typeRefLabel = "FROM OUTER SPACE PASS",
                    validityStartDate = LocalDate.of(2019, 12, 23),
                    validityEndDate = LocalDate.of(2999, 12, 31)
                )
            )
        )
        `when`(customerService.getCustomerInfo(eq("trotro")))
            .thenReturn(customer)
        val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Authorization", "Bearer $accessToken")
            .accept(APPLICATION_JSON)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("""
                {
                    "customerId":"trotro",
                    "firstName":"Ane",
                    "lastName":"Trotro",
                    "phoneNumber":"06-07-08-09-10",
                    "email":"ane.trotro@rigo.lo",
                    "loyaltyProgram":{"number":"007","label":"CD7F32","validityStartDate":"2000-01-01","validityEndDate":null},
                    "railPasses":[{"number":"TROID","label":"FROM OUTER SPACE PASS","validityStartDate":"2019-12-23","validityEndDate":"2999-12-31"}]
                }
                """.trimIndent())
        verify(customerService).getCustomerInfo(eq("trotro"))
    }

    @Test
    fun `GET customers should return 401 for unauthorized user`(): Unit = runBlocking {
        // Given no authentication

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Accept", APPLICATION_JSON_VALUE)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isUnauthorized
        verify(customerService, never()).getCustomerInfo(anyString())
    }

    @Test
    fun `GET customers should return 401 if principal is not a principal token`(): Unit = runBlocking {
        //given no real token

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Authorization", "Bearer Mouahahahaha")
            .header("Accept", APPLICATION_JSON_VALUE)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isUnauthorized

        verify(customerService, never()).getCustomerInfo(anyString())
    }

    @Test
    fun `GET customers should return 403 forbidden when authenticated user with insufficient privileges`(): Unit = runBlocking {
        // Given
        val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.none")

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", APPLICATION_JSON_VALUE)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isForbidden
        verify(customerService, never()).getCustomerInfo(anyString())
    }

    @Test
    fun `GET customers should return 404 if no customer info has been found`(): Unit = runBlocking {
        //Given
        `when`(customerService.getCustomerInfo("trotro"))
            .thenThrow(NotFoundException("trotro", "Ane"))
        val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Authorization", "Bearer $accessToken")
            .accept(APPLICATION_JSON)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isNotFound
            .expectBody().json("""
              {
                "code":"NOT_FOUND",
                "message":"No result for the given Ane id=trotro"
              }
            """.trimIndent())

        verify(customerService).getCustomerInfo(eq("trotro"))
    }

    @Test
    fun `GET customers should return 500 if something terrible happened`(): Unit = runBlocking {
        //Given
        `when`(customerService.getCustomerInfo(eq("trotro")))
            .thenThrow(RuntimeException("Mouhahahaha >:)"))

        val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")

        //Test & Assert
        webTestClient.get()
            .uri("/customers")
            .header("Authorization", "Bearer $accessToken")
            .accept(APPLICATION_JSON)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().is5xxServerError
        verify(customerService).getCustomerInfo(eq("trotro"))
    }

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