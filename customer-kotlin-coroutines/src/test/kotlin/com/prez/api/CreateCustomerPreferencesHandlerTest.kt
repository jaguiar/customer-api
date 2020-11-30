package com.prez.api

import com.prez.api.dto.CreateCustomerPreferencesRequest
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference.NEAR_CORRIDOR
import com.prez.model.SeatPreference.NEAR_WINDOW
import com.prez.model.SeatPreference.NO_PREFERENCE
import com.prez.service.CustomerService
import com.prez.utils.FakeTokenGenerator
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.startsWith
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.Locale.FRENCH

@SpringBootTest
@AutoConfigureWebTestClient
internal class CreateCustomerPreferencesHandlerTest(@Autowired private val webTestClient: WebTestClient) {

  @MockBean
  lateinit var customerService: CustomerService

  private val fakeTokenGenerator = FakeTokenGenerator("test-authorization-server")

  @Test
  @Throws(Exception::class)
  fun `POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write")
      val toCreate = CreateCustomerPreferencesRequest(
        seatPreference = NO_PREFERENCE,
        classPreference = 1,
        profileName = "Trotro",
        language = "fr"
      )
      `when`(customerService.saveCustomerPreferences("Ane", NO_PREFERENCE, 1, "Trotro", FRENCH))
        .thenReturn(
          CustomerPreferences(
            id = "ane.trotro@rigo.lo",
            customerId = "Ane",
            seatPreference = NO_PREFERENCE,
            classPreference = 1,
            profileName = "Trotro"
          )
        )

      // When && Then
      webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(toCreate))
        .exchange()
        .expectStatus().isCreated
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json(
          """
                  {
                    "id":"ane.trotro@rigo.lo",
                    "customerId":"Ane",
                    "seatPreference":"NO_PREFERENCE",
                    "classPreference":1,
                    "profileName":"Trotro"
                  }
                  """.trimIndent()
        )
      verify(customerService)
        .saveCustomerPreferences("Ane", NO_PREFERENCE, 1, "Trotro", FRENCH)
    }

  @Test
  @Throws(Exception::class)
  fun `POST customers preferences should return OK when customer preferences successfully created for authorized user with valid input and null language`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write")
      val toCreate = CreateCustomerPreferencesRequest(
        seatPreference = NO_PREFERENCE,
        classPreference = 1,
        profileName = "Trotro"
      )
      `when`(customerService.saveCustomerPreferences("Ane", NO_PREFERENCE, 1, "Trotro", null))
        .thenReturn(
          CustomerPreferences(
            id = "ane.trotro@rigo.lo",
            customerId = "Ane",
            seatPreference = NO_PREFERENCE,
            classPreference = 1,
            profileName = "Trotro"
          )
        )

      // When && Then
      webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(toCreate))
        .exchange()
        .expectStatus().isCreated
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json(
          """
                  {
                    "id":"ane.trotro@rigo.lo",
                    "customerId":"Ane",
                    "seatPreference":"NO_PREFERENCE",
                    "classPreference":1,
                    "profileName":"Trotro"
                  }
                  """.trimIndent()
        )
      verify(customerService)
        .saveCustomerPreferences("Ane", NO_PREFERENCE, 1, "Trotro", null)
    }

  @Test
  @Throws(Exception::class)
  fun `POST customers preferences should return 400 bad request when language is not valid`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_WINDOW,
      classPreference = 2,
      profileName = "PasAssezPasAssez",
      language = "bleh"
    )

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
      .jsonPath("$.message")
      .value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
      .jsonPath("$.message").value(containsString("The language is not valid. Accepted languages are : fr,de,es,en,it,pt"))

    // FIXME noteworthy https://discuss.kotlinlang.org/t/how-to-use-mockito-with-kotlin/324/13
    // any() must not be null at the second argument
    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 403 forbidden when not authenticated user`(): Unit = runBlocking {
    // Given no authentication
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_WINDOW,
      classPreference = 2,
      profileName = "PasAssezPasAssez"
    )

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isForbidden
      .expectBody(String::class.java)
      .isEqualTo<Nothing>("CSRF Token has been associated to this client")
    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 403 forbidden when authenticated user with insufficient privileges`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")
      val toCreate = CreateCustomerPreferencesRequest(
        seatPreference = NEAR_CORRIDOR,
        classPreference = 2,
        profileName = "PasAssezPasAssez"
      )

      // When && Then
      webTestClient.post().uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(toCreate))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().json("")
      verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
    }


  @Test
  fun `POST customers preferences should return 400 bad request when seat preference is null`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = """
                        { "classPreference":"2",
                        "profileName":"",
                        "language":"it"
                        }
                        """.trimIndent()

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
    // Here we don't get the fancy message because of DecodingException due to non-null field

    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST  customerspreferences should return 400 bad request when classPreference is not valid`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_CORRIDOR,
      profileName = "PasAssezPasAssez",
      language = "de",
      classPreference = 12
    )

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
    // Here we don't get the fancy message because of DecodingException due to non-null field
    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 400 bad request when classPreference is not valid`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_CORRIDOR,
      classPreference = 3,
      profileName = "PasAssezPasAssez",
      language = "en"
    )

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
      .jsonPath("$.message")
      .value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
      .jsonPath("$.message").value(containsString("Max value for class preference is 2"))

    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 400 bad request when profileName is null`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = // TODO Note Autre différence avec la version Java
      """
            {
            "seatPreference":"NEAR_CORRIDOR",
            "classPreference":"2",
            "language":"fr"
            }
            """.trimIndent()

    // When && Then
    webTestClient.post()
      .uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("{\"code\":\"VALIDATION_ERROR\"}")
    // Here we don't get the fancy message because of DecodingException due to non-null field

    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), isNull())
  }

  @Test
  fun `POST customers preferences should return 400 bad request when profileName does not respect pattern`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
      val toCreate = CreateCustomerPreferencesRequest(
        seatPreference = NEAR_WINDOW,
        classPreference = 2,
        profileName = "???!PasAssezPasAssez???",
        language = "fr"
      )

      // When && Then
      webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(toCreate))
        .exchange()
        .expectStatus().isBadRequest
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody().json("""{"code":"VALIDATION_ERROR"}""")
        .jsonPath("$.message")
        .value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(containsString("The profile name contains forbidden characters"))
      verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
    }

  @Test
  fun `POST customers preferences should return 400 bad request when profileName is empty`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_WINDOW,
      classPreference = 2,
      profileName = "",
      language = "fr"
    )

    // When && Then
    webTestClient.post().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("""{"code":"VALIDATION_ERROR"}""")
    // Here we don't get the fancy message because of DecodingException due to non-null field
    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 400 bad request when profileName is too long`(): Unit = runBlocking {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.write")
    val toCreate = CreateCustomerPreferencesRequest(
      seatPreference = NEAR_WINDOW,
      classPreference = 2,
      profileName =
      "blablablablablablablablablbalbalbalbalblablablablablablablablablablablablablablablbalbalbalbalbalbalbalablablbalbalbalbalbalbalbalbalbalbalbalbalbalbalblabalbalbalbablablablablablablabla",
      language = "fr"
    )

    // When && Then
    webTestClient.post()
      .uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromValue(toCreate))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
      .expectBody().json("""{"code":"VALIDATION_ERROR"}""")
      .jsonPath("$.message")
      .value(startsWith("1 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
      .jsonPath("$.message").value(containsString("The profile name should have a size between 1 and 50 characters"))
    verify(customerService, never()).saveCustomerPreferences(anyString(), anyObject(), anyInt(), anyString(), any())
  }

  @Test
  fun `POST customers preferences should return 500 internal server error if something goes horribly wrong`(): Unit =
    runBlocking {
      // Given
      val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("boom", 3600, "customer.write")
      val toCreate = CreateCustomerPreferencesRequest(
        seatPreference = NEAR_WINDOW,
        classPreference = 2,
        profileName = "Boom",
        language = "fr"
      )

      `when`(customerService.saveCustomerPreferences("boom", NEAR_WINDOW, 2, "Boom", FRENCH))
        .thenThrow(RuntimeException("Boom badaboum!!!"))

      // When && Then
      webTestClient.post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $accessToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(toCreate))
        .exchange()
        .expectStatus().is5xxServerError
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody().json("""
        {
          "code":"UNEXPECTED_ERROR",
          "message":"Something horribly wrong happened, I could tell you what but then I’d have to kill you."
        }
        """.trimIndent())
    }

  /**
   * FIXME
   * Look at this thread for more explanation of this weird thing
   * https://discuss.kotlinlang.org/t/how-to-use-mockito-with-kotlin/324
   */
  private fun <T> anyObject(): T {
    Mockito.any<T>()
    return uninitialized()
  }

  private fun <T> uninitialized(): T = null as T

}