package com.prez.lib.health

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient

class ExternalServiceHealthIndicator(
  private val serviceName: String,
  private val serviceUrl: String,
  connectTimeoutInMs: Int = 1000,
  readTimeoutInSeconds: Int = 1
) : ReactiveHealthIndicator {

  private val webClient: WebClient =
    WebClient.builder()
      .baseUrl(serviceUrl)
      .clientConnector(
        ReactorClientHttpConnector(
          HttpClient.create()
            .tcpConfiguration { client: TcpClient ->
              client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutInMs)
                .doOnConnected { conn: Connection -> conn.addHandlerLast(ReadTimeoutHandler(readTimeoutInSeconds)) }
            })
      )
      .build()

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ExternalServiceHealthIndicator::class.java)
  }

  override fun health(): Mono<Health> {
    return check().onErrorResume { ex: Throwable ->
      LOGGER.info("Cannot connect to $serviceName ($serviceUrl). Exception: $ex")
      Mono.just(
        Health.Builder().down().withDetail("name", serviceName).withDetail("error", ex.message ?: ex.toString()).build()
      )
    }
  }

  private fun check(): Mono<Health> {
    return webClient.head()
      .retrieve()
      .bodyToMono(
        String::class.java
      ) // we need to consume the body if any if we do not want to die a terrible death : https://docs.spring.io/spring/docs/5.1.5.RELEASE/spring-framework-reference/web-reactive.html#webflux-client-retrieve
      .defaultIfEmpty("") //to handle no content body
      .flatMap { _: String ->
        Mono.just(
          Health.Builder().up().withDetail("name", serviceName).withDetail("url", serviceUrl).build()
        )
      }
  }


}