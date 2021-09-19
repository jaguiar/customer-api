package com.prez.lib.health

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.head
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import java.time.Duration

@Tag("integration")
class ExternalServiceHealthIndicatorIntegrationTest {

  companion object {
    private const val INDICATOR_NAME = "LaCompagnieCreole"
    private val TIMEOUT = Duration.ofSeconds(5)
    private val wireMockRule = WireMockRule(
      options()
        .dynamicPort()
    )

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      wireMockRule.start()
      configureFor("localhost", wireMockRule.port())
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      wireMockRule.shutdown()
    }
  }

  private val toTest = ExternalServiceHealthIndicator(INDICATOR_NAME, "http://localhost:${wireMockRule.port()}")

  @Test
  fun `health shouldReturn Health when Health is UP`() {
    //Arrange
    wireMockRule.stubFor(
      head(urlEqualTo("/"))
        .willReturn(aResponse().withBody("Decalecatan, decalecatan, ohe, ohe !").withStatus(200))
    )

    //Act
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertEquals(Status.UP, result?.status)
    assertEquals(INDICATOR_NAME, result?.details?.get("name"))
    assertEquals("http://localhost:${wireMockRule.port()}", result?.details?.get("url"))
  }

  @Test
  fun `health shouldReturn Health when Health is DOWN (5xx)`() {
    //Arrange
    wireMockRule.stubFor(
      head(urlEqualTo("/")).willReturn(aResponse().withBody("I'm dead").withStatus(500))
    )

    //Act
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertEquals(Status.DOWN, result?.status)
    assertEquals(INDICATOR_NAME, result?.details?.get("name"))
    assertEquals(
      "500 Internal Server Error from HEAD http://localhost:${wireMockRule.port()}",
      result?.details?.get("error")
    )
  }

  @Test
  fun `health shouldReturn Health when Health is DOWN (4xx)`() {
    //Arrange
    wireMockRule.stubFor(
      head(urlEqualTo("/")).willReturn(aResponse().withStatus(404))
    )

    //Act
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertEquals(Status.DOWN, result?.status)
    assertEquals(INDICATOR_NAME, result?.details?.get("name"))
    assertEquals("404 Not Found from HEAD http://localhost:${wireMockRule.port()}", result?.details?.get("error"))
  }

  @Test
  fun `health shouldReturn Health when there is a timeout`() {
    //Arrange
    wireMockRule.stubFor(
      head(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(3000))
    )

    //Act
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertEquals(Status.DOWN, result?.status)
    assertEquals(INDICATOR_NAME, result?.details?.get("name"))
    assertTrue((result?.details?.get("error") as String).contains("io.netty.handler.timeout.ReadTimeoutException"))
  }

  @Test
  fun `health shouldReturn Health when the body is empty`() {
    //Arrange
    wireMockRule.stubFor(
      head(urlEqualTo("/")).willReturn(aResponse().withStatus(200))
    )

    //Act
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertEquals(Status.UP, result?.status)
    assertEquals(INDICATOR_NAME, result?.details?.get("name"))
    assertEquals("http://localhost:${wireMockRule.port()}", result?.details?.get("url"))
  }
}