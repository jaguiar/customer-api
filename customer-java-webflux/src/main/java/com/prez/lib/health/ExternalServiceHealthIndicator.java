package com.prez.lib.health;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class ExternalServiceHealthIndicator implements ReactiveHealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalServiceHealthIndicator.class);

  private String serviceName;
  private String serviceUrl;
  private WebClient webClient;

  public ExternalServiceHealthIndicator(String serviceName, String serviceUrl) {
    this(serviceName, serviceUrl, 1000, 1);
  }

  public ExternalServiceHealthIndicator(String serviceName, String serviceUrl, int connectTimeoutInMs,
                                        int readTimeoutInSeconds) {
    HttpClient httpClient = HttpClient.create()
        .tcpConfiguration(client ->
            client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMs)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(readTimeoutInSeconds)))
        );
    webClient = WebClient.builder()
        .baseUrl(serviceUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .build();
    this.serviceName = serviceName;
    this.serviceUrl = serviceUrl;
  }

  @Override
  public Mono<Health> health() {
    return check().onErrorResume( // handle 4xx and 5xx errors and timeouts
        ex -> {
          LOGGER.info("Cannot connect to {} ({}). Exception: {}", serviceName, serviceUrl, ex);
          final String errorMessage = defaultIfBlank(ex.getMessage(), ex.toString());
          return Mono
              .just(new Health.Builder().down().withDetail("name", serviceName).withDetail("Error", errorMessage).build());
        });
  }

  private Mono<Health> check() {
    return webClient.head()
        .retrieve()
        .bodyToMono(
            String.class) // we need to consume the body if any if we do not want to die a terrible death : https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-framework-reference/web-reactive.html#webflux-client-retrieve
        .defaultIfEmpty("")//to handle no content body
        .flatMap(b -> Mono.just(new Health.Builder().up().withDetail("name", serviceName).build()));
  }

}