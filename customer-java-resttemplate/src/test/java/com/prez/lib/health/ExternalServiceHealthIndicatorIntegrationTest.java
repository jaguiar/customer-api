package com.prez.lib.health;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@Tag("integration")
class ExternalServiceHealthIndicatorIntegrationTest {

  private final static String INDICATOR_NAME = "LaCompagnieCreole";


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
    Health result = toTest.health();

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
    Health result = toTest.health();

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals((String) result.getDetails().get("url"), "http://localhost:" + wireMockRule.port());
    assertThat((String) result.getDetails().get("error")).contains("500 Server Error");
  }


  @Test
  @DisplayName("health shouldReturn Health when Health is DOWN (4xx)")
  void health_shouldReturnHealth_whenHealthIsDown_4xx() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withStatus(404))
    );

    //Act
    Health result = toTest.health();

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertThat((String) result.getDetails().get("url"))
        .contains("http://localhost:" + wireMockRule.port());
    assertThat((String) result.getDetails().get("error"))
        .contains("404 Not Found");
  }

  @Test
  @DisplayName("health shouldReturn Health when there is a timeout")
  void health_shouldReturnHealth_whenTimeout() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(3000))
    );

    //Act
    Health result = toTest.health();

    //Assert
    assertEquals(Status.DOWN, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    String error = (String) result.getDetails().get("error");
    assertThat(error).contains("Read timed out");
  }

  @Test
  @DisplayName("health shouldReturn Health when the body is empty")
  void health_shouldReturnHealth_whenEmptyBody() {
    //Arrange
    wireMockRule.stubFor(
        head(urlEqualTo("/")).willReturn(aResponse().withStatus(200))
    );

    //Act
    Health result = toTest.health();

    //Assert
    assertEquals(Status.UP, result.getStatus());
    assertEquals(INDICATOR_NAME, result.getDetails().get("name"));
    assertEquals("http://localhost:" + wireMockRule.port(), result.getDetails().get("url"));
  }
}