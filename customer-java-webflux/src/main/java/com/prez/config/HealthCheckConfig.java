package com.prez.config;

import com.prez.lib.health.AcceptableMeanIndicator;
import com.prez.lib.health.ExternalServiceHealthIndicator;
import io.micrometer.core.instrument.DistributionSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckConfig {

  // External webservice
  @Bean
  ReactiveHealthIndicator customerSourceHistoHealthIndicator(DistributionSummary customerWSSummary,
                                                     @Value("${customer.ws.acceptable.mean:0.98}") Double acceptableMean) {
    return new AcceptableMeanIndicator(customerWSSummary, acceptableMean, "CustomerSource");
  }

  @Bean
  ReactiveHealthIndicator customerSourceUrlHealthIndicator(@Value("${customer.ws.base-path}") String url) {
    return new ExternalServiceHealthIndicator("customer source", url);
  }

  // Mongo & Redis have out-of-the-box indicators available
}