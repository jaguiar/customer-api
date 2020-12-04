package com.prez.api

import com.prez.exception.NotFoundException
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference
import com.prez.service.CustomerService
import com.prez.utils.FakeTokenGenerator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient


@SpringBootTest
@AutoConfigureWebTestClient
internal class GetCustomerPreferencesHandlerTest(@Autowired val webTestClient: WebTestClient) {

  @MockBean
  private lateinit var customerService: CustomerService

  private val fakeTokenGenerator = FakeTokenGenerator("test-authorization-server")

  @Test
  fun `GET customers preferences should return 401 unauthorized when not authenticated user`(): Unit = runBlocking {
    // Given no authentication

    // When && Then
    webTestClient.get().uri("/customers/preferences")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .isEmpty
    verify(customerService, never()).getCustomerPreferences(ArgumentMatchers.anyString())
  }

  @Test
  fun `GET customers preferences should return 403 forbidden when authenticated user with insufficient privileges`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "not.enough")

      // When && Then
      webTestClient.get().uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().json("")
      verify(customerService, never()).getCustomerPreferences(ArgumentMatchers.anyString())
    }

  // Given
  @Test
  fun `GET customers preferences should return not found when no preferences`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")
    `when`(customerService.getCustomerPreferences("trotro")).thenThrow(NotFoundException("trotro", "Ane"))

    // When && Then
    webTestClient.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectBody().json(
        """
          {
            "code":"NOT_FOUND",
            "message":"No result for the given Ane id=trotro"
          }
        """.trimIndent()
      )
  }

  @Test
  fun `GET customers preferences should return OK when found preferences`(): Unit = runBlocking {
    // When && Then
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")
    `when`(customerService.getCustomerPreferences("trotro"))
      .thenReturn(
        listOf(
          CustomerPreferences(
            customerId = "trotro",
            profileName = "rigolo",
            seatPreference = SeatPreference.NO_PREFERENCE,
            classPreference = 2
          )
        )
      )

    // When && Then
    webTestClient.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().json(
        """
            {
              "profiles":[{
                "customerId":"trotro",
            "seatPreference":"NO_PREFERENCE",
            "classPreference":2,
                "profileName":"rigolo"
              }]
            }
            """.trimIndent()
      )

    verify(customerService).getCustomerPreferences("trotro")
  }

  @Test
  fun `GET customers preferences should return internal server error when something went wrong`(): Unit = runBlocking {
    // When && Then
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("boom", 3600, "customer.read")
    `when`(customerService.getCustomerPreferences("boom"))
      .thenThrow(RuntimeException("Boom badaboum"))

    // When && Then
    webTestClient.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody().json(
        """
        {
          "code":"UNEXPECTED_ERROR",
          "message":"Something horribly wrong happened, I could tell you what but then I’d have to kill you."
        }
        """.trimIndent()
      )
  }
}