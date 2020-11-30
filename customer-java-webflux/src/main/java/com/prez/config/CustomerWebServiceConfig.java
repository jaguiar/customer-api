package com.prez.config;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import brave.SpanCustomizer;
import com.prez.lib.tracing.ResponseMarkerFilter;
import com.prez.lib.tracing.SpanCustomizationWebClientFilter;
import com.prez.ws.CustomerWSClient;
import com.prez.ws.CustomerWSProperties;
import io.micrometer.core.instrument.DistributionSummary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(CustomerWSProperties.class)
public class CustomerWebServiceConfig {


  @Bean
  public CustomerWSClient customerWsClient(CustomerWSProperties properties,
                                           @Qualifier("customerWSSummary") DistributionSummary customerWSSummary,
                                           SpanCustomizer spanCustomizer) {
    WebClient webClient = WebClient.builder()
        .defaultHeader(ACCEPT, APPLICATION_JSON_VALUE)
        .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .filter(new SpanCustomizationWebClientFilter("CustomerWS", spanCustomizer))
        .filter(new ResponseMarkerFilter(customerWSSummary))
        .build();

    return new CustomerWSClient(properties, webClient);
  }
}
