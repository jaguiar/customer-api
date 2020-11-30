package com.prez.lib.health;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class ExternalServiceHealthIndicator implements HealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServiceHealthIndicator.class);

  private String serviceName;
  private String serviceUrl;
  private RestTemplate webClient;

  public ExternalServiceHealthIndicator(String serviceName, String serviceUrl) {
    this(serviceName, serviceUrl, 1000, 1);
  }

  public ExternalServiceHealthIndicator(String serviceName, String serviceUrl, int connectTimeoutInMs,
                                        int readTimeoutInSeconds) {
    webClient = new RestTemplateBuilder()
        .setConnectTimeout(ofMillis(connectTimeoutInMs))
        .setReadTimeout(ofSeconds(readTimeoutInSeconds))
        .rootUri(serviceUrl)
        .build();
    this.serviceName = serviceName;
    this.serviceUrl = serviceUrl;
  }

  @Override
  public Health health() {
    try {
      return check();
    } catch (Exception ex) {
      // handle 4xx and 5xx errors and timeouts
      LOGGER.info("Cannot connect to {} ({}). Exception: {}", serviceName, serviceUrl, ex);
      final String errorMessage = defaultIfBlank(ex.getMessage(), ex.toString());
      return new Health.Builder().down().withDetail("name", serviceName).withDetail("Error", errorMessage).build();
    }
  }

  private Health check() {
    webClient.headForHeaders("/");
    return new Health.Builder().up().withDetail("name", serviceName).build();
  }

}