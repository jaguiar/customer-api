package com.prez.config

import brave.SpanCustomizer
import com.prez.lib.tracing.ResponseMarkerFilter
import com.prez.lib.tracing.SpanCustomizationWebClientFilter
import com.prez.ws.CustomerWSClient
import com.prez.ws.CustomerWSProperties
import io.micrometer.core.instrument.DistributionSummary
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.client.WebClient
import javax.validation.constraints.NotBlank

@Configuration
@ConfigurationProperties(prefix = "customer.ws")
class CustomerWebServiceConfig {

  @NotBlank
  lateinit var url: String

  @Bean
  internal fun CustomerWebServiceConfig(): CustomerWSProperties {
    return CustomerWSProperties(
      url = url
    )
  }

  @Bean
  internal fun customerWsClient(
    properties: CustomerWSProperties,
    customerWSSummary: DistributionSummary,
    spanCustomizer: SpanCustomizer
  ): CustomerWSClient {
    val webClient = WebClient.builder()
      .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
      .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
      .filter(SpanCustomizationWebClientFilter("CustomerWS", spanCustomizer))
      .filter(ResponseMarkerFilter("CustomerWS", customerWSSummary))
      .build()

    return CustomerWSClient(properties, webClient)
  }
}
