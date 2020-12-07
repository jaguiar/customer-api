package com.prez.config;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean(name = "customerWSSummary")
  public DistributionSummary customerSourceSummary(MeterRegistry metricRegistry) {
    return DistributionSummary.builder("customer.webservice.get")
        .description("Error/success ratio for customer web service call")
        .distributionStatisticBufferLength(5)
        .distributionStatisticExpiry(Duration.ofMinutes(5))
        .minimumExpectedValue(1.0)
        .minimumExpectedValue(Double.MAX_VALUE)
        .publishPercentileHistogram(true)
        .percentilePrecision(1)
        .register(metricRegistry);
  }

  @Bean
  public MeterRegistry simpleMeterRegistry() {
    return new SimpleMeterRegistry();
  }

}
