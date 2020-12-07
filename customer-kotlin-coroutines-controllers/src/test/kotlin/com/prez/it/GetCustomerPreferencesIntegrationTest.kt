package com.prez.it

import com.prez.UsingMongoDBAndRedis
import com.prez.model.Customer
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference.NEAR_WINDOW
import com.prez.model.SeatPreference.NO_PREFERENCE
import com.prez.utils.FakeTokenGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient


@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["test"])
internal class GetCustomerPreferencesIntegrationTest(
  @Autowired private val client: WebTestClient,
  @Autowired private val customerInfoRedisTemplate: ReactiveRedisTemplate<String, Customer>,
  @Autowired private val mongoOperations: ReactiveMongoOperations
) : UsingMongoDBAndRedis() {

  private val fakeTokenGenerator = FakeTokenGenerator("test-authorization-server")

  @BeforeEach
  fun beforeEach() {
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block()
    mongoOperations.dropCollection(CustomerPreferences::class.java).block()
  }
  // Given no authentication

  // When && Then
  @Test
  fun `should return 401 unauthorized when not authenticated user`() {
    // Given no authentication

    // When && Then
    client.get().uri("/customers/preferences")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isUnauthorized
      .expectBody()
      .isEmpty
  }

  // Given
  @Test
  fun `should return 403 forbidden when authenticated user with insufficient privileges`() {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "not.enough")

    // When && Then
    client.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isForbidden
      .expectBody().json("")
  }

  // Given
  @Test
  fun `should return not found when no existing customer preferences`() {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")

    // When && Then
    client.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
      .expectBody().json("""
          {
            "code":"NOT_FOUND",
            "message":"No result for the given customer id=trotro"
          }
          """.trimIndent())
  }

  // Given
  @Test
  fun `should return OK when found customer preferences`() {
    // Given
    val accessToken = fakeTokenGenerator.generateNotExpiredSignedToken("trotro", 3600, "customer.read")
    mongoOperations.save(
      CustomerPreferences(
        customerId = "trotro",
        profileName = "rigolo",
        seatPreference = NO_PREFERENCE,
        classPreference = 2
      )
    ).block()
    mongoOperations.save(
      CustomerPreferences(
        customerId = "trotro",
        profileName = "drole",
        seatPreference = NEAR_WINDOW,
        classPreference = 1
      )
    ).block()

    // When && Then
    client.get().uri("/customers/preferences")
      .header("Authorization", "Bearer $accessToken")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().json(
        """
            [
              {
                "customerId":"trotro",
                "profileName":"rigolo",
                "seatPreference":"NO_PREFERENCE",
                "classPreference":2},
              {
                "customerId":"trotro",
                "profileName":"drole",
                "seatPreference":"NEAR_WINDOW",
                "classPreference":1
              }]
        """.trimIndent()
      )

  }
}
