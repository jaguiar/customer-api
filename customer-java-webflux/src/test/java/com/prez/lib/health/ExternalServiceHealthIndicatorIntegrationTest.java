package com.prez.lib.health;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class ExternalServiceHealthIndicatorIntegrationTest {

  private final static String INDICATOR_NAME = "LaCompagnieCreole";
  private final static Duration TIMEOUT = Duration.ofSeconds(5);

  private static final WireMockRule wireMockRule = new WireMockRule(options()
      .dynamicPort());

  @BeforeAll
  static void beforeAll() {
    wireMockRule.start();
    configureFor("localhost", wireMockRule.port());

  }

  @AfterAll
  static void afterAll() {
    wireMockRule.shutdown();
  }

  private ExternalServiceHealthIndicator toTest = new ExternalServiceHealthIndicator(INDICATOR_NAME, "http://localhost:" + wireMockRule.port());

  @Test
  @DisplayName("health shouldReturn Health when Health is UP")
  void health_shouldReturnHealth_whenHealthIsUp() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withBody("Decalecatan, decalecatan, ohe, ohe !").withStatus(200))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.UP, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("http://localhost:" + wireMockRule.port(), result.getDetails().get("url"));

  }

  @Test
  @DisplayName("health shouldReturn Health when Health is DOWN (5xx)")
  void health_shouldReturnHealth_whenHealthIsDown5xx() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withBody("I'm dead").withStatus(500))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("500 Internal Server Error from HEAD http://localhost:" + wireMockRule.port(),
        result.getDetails().get("error"));
  }


  @Test
  @DisplayName("health shouldReturn Health when Health is DOWN (4xx)")
  void health_shouldReturnHealth_whenHealthIsDown_4xx() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withStatus(404))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("404 Not Found from HEAD http://localhost:" + wireMockRule.port(), result.getDetails().get("error"));
  }

  @Test
  @DisplayName("health shouldReturn Health when there is a timeout")
  void health_shouldReturnHealth_whenTimeout() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(3000))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    String actualError = (String) result.getDetails().get("error");
    assertTrue(StringUtils.contains(actualError, "io.netty.handler.timeout.ReadTimeoutException"));
  }

  @Test
  @DisplayName("health shouldReturn Health when the body is empty")
  void health_shouldReturnHealth_whenEmptyBody() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withStatus(200))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.UP, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("http://localhost:" + wireMockRule.port(), result.getDetails().get("url"));
  }
}