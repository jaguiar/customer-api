package com.prez.it

import com.prez.UsingMongoDBAndRedis
import com.prez.model.Customer
import com.prez.utils.FakeTokenGenerator
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.FluentMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@Tag("docker")
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["test"])
class CreateCustomerPreferencesIntegrationTest(
    @Autowired val client: WebTestClient,
    @Autowired val customerInfoRedisTemplate: ReactiveRedisTemplate<String, Customer>,
    @Autowired val mongoOperations: ReactiveMongoOperations
) : UsingMongoDBAndRedis() {
  companion object {
    private val fakeTokenGenerator: FakeTokenGenerator = FakeTokenGenerator("test-authorization-server")
    private val validToken =
        fakeTokenGenerator.generateNotExpiredSignedToken("subzero", 3600, "customer.read customer.write")
  }

  @BeforeEach
  fun beforeEach() {
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block()
    mongoOperations.dropCollection("preferences")
  }

  @Test
  fun `should return Bad Request when missing mandatory fields`() {
    // Given a user with a valid access token

    // When
    val toCreate = """
      {"seatPreference":"NEAR_WINDOW"}
      """.trimIndent()

    // Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $validToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(toCreate)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
        // Here we don't get the fancy message because of DecodingException due to non-null field
  }

  @Test
  fun `should return Bad Request when invalid input values`() {
    // Given a user with a valid access token

    // When
    val toCreate = """
      {
        "seatPreference":"NEAR_CORRIDOR",
        "classPreference":23,
        "profileName":"Trotro*",
        "language":"meh"
      }
      """.trimIndent()

    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $validToken")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(toCreate)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
        .jsonPath("$.message").value(Matchers.startsWith("3 error(s) while validating com.prez.api.dto.CreateCustomerPreferencesRequest : "))
        .jsonPath("$.message").value(Matchers.containsString("The profile name contains forbidden characters"))
        .jsonPath("$.message").value(Matchers.containsString("Max value for class preference is 2"))
        .jsonPath("$.message").value(Matchers.containsString("The language is not valid. Accepted languages are : fr,de,es,en,it,pt"))
    // Then
  }

  @Test
  fun `should return 403 forbidden when not authenticated user`() {
    // Given no authentication
    val validRequest = """
      {
        "seatPreference":"NEAR_WINDOW",
        "classPreference":2,
        "profileName":"PasAssezPasAssez",
        "language":"fr"
      }
      """.trimIndent()

    // When && Then
    client
        .post()
        .uri("/customers/preferences")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(validRequest)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
  }

  @Test
  fun `should return 403 forbidden when authenticated user with insufficient privileges`() {
    // Given
    val insufficientPrivileges: String = fakeTokenGenerator.generateNotExpiredSignedToken("cached", 3600, "customer.read")
    val validRequest = """
      {
        "seatPreference":"NEAR_WINDOW",
        "classPreference":2,
        "profileName":"PasAssezPasAssez",
        "language":"fr"
      }
      """.trimIndent()

    // When && Then
    client
        .post()
        .uri("/customers/preferences")
        .header("Authorization", "Bearer $insufficientPrivileges")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(validRequest)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
  }

  @Test
  fun `should return created preferences when customer preferences successfully created for authorized user with valid input`() {
    // Given
    val validToken: String = fakeTokenGenerator.generateNotExpiredSignedToken("Ane", 3600, "customer.write")
    val validRequest = """
      {
        "seatPreference":"NO_PREFERENCE",
        "classPreference":1,
        "profileName":"Trotro",
        "language":"fr"
      }
      """.trimIndent()

    // When && Then
    client
      .post()
      .uri("/customers/preferences")
      .header("Authorization", "Bearer $validToken")
      .accept(MediaType.APPLICATION_JSON)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(validRequest)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isCreated
      .expectBody().json("""
        {
          "seatPreference":"NO_PREFERENCE",
          "classPreference":1,
          "profileName":"Trotro",
          "language":"fr"
        }
        """.trimEnd()
        )
  }

  // TODO add other tests ?
}