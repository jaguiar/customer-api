package com.prez.lib.health;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@Tag("integration")
public class ExternalServiceHealthIndicatorTest {

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
  public void health_shouldReturnHealth_whenHealthIsUp() {
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
  public void health_shouldReturnHealth_whenHealthIsDown5xx() {
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
  public void health_shouldReturnHealth_whenHealthIsDown_4xx() {
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
  public void health_shouldReturnHealth_whenTimeout() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(3000))
    );

    //Act
    Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("io.netty.handler.timeout.ReadTimeoutException", result.getDetails().get("error"));
  }

  @Test
  @DisplayName("health shouldReturn Health when the body is empty")
  public void health_shouldReturnHealth_whenEmptyBody() {
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