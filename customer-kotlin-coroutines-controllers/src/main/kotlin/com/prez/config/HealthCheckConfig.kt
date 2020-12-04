package com.prez.config

import com.prez.lib.health.AcceptableMeanIndicator
import com.prez.lib.health.ExternalServiceHealthIndicator
import io.micrometer.core.instrument.DistributionSummary
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HealthCheckConfig {

  // External webservice
  @Bean("customerSourceHistoHealthIndicator")
  /* you need to set a name or the method name will be suffixed by the app name */
  internal fun customerSourceHistoHealthIndicator(
      customerWSSummary: DistributionSummary,
      @Value("\${customer.ws.acceptable.mean:0.98}") acceptableMean: Double
  ): ReactiveHealthIndicator? {
    return AcceptableMeanIndicator(customerWSSummary, acceptableMean, "CustomerSource")
  }

  @Bean("customerSourceUrlHealthIndicator")
  /* you need to set a name or the method name will be suffixed by the app name */
  internal fun customerSourceUrlHealthIndicator(@Value("\${customer.ws.base-path}") url: String): ReactiveHealthIndicator? {
    return ExternalServiceHealthIndicator("customer source", url)
  }

  // Mongo & Redis have out-of-the-box indicators available

}