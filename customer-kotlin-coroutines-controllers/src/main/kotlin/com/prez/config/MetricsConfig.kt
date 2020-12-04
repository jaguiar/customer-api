package com.prez.config

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class MetricsConfig {

    @Bean(name = ["customerWSSummary"])
    fun customerSourceSummary(metricRegistry: MeterRegistry): DistributionSummary {
        // FIXME config ~xxxx requêtes pour un serveur en heure
        return DistributionSummary.builder("customer.webservice.get")
            .description("Error/success ratio for customer web service call")
            .distributionStatisticBufferLength(5)
            .distributionStatisticExpiry(Duration.ofMinutes(5))
            .minimumExpectedValue(1.0)
            .minimumExpectedValue(Double.MAX_VALUE)
            .publishPercentileHistogram(true)
            .percentilePrecision(1)
            .register(metricRegistry)
    }

  @Bean
  fun simpleMeterRegistry(): MeterRegistry {
    return SimpleMeterRegistry()
  }
}
