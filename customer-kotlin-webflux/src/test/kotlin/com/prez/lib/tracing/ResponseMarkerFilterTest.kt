package com.prez.lib.tracing

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI

class ResponseMarkerFilterTest {

  private lateinit var toTest: ResponseMarkerFilter
  private val simpleMeterRegistry = SimpleMeterRegistry()
  private lateinit var summary: DistributionSummary

  @BeforeEach
  fun beforeEach() {
    summary = DistributionSummary.builder("testCustomerWS").register(simpleMeterRegistry)
    toTest = ResponseMarkerFilter("webService", summary)
  }

  @AfterEach
  fun afterEach() {
    simpleMeterRegistry.remove(summary)
  }

  @Test
  fun `should update histogram with value 1 when request execution succeeded`() {
    // Given
    val successExecution = ExchangeFunction { Mono.just(ClientResponse.create(HttpStatus.OK).build()) }
    val clientRequest = ClientRequest.create(HttpMethod.GET, URI("http://url")).build()

    // When
    toTest.filter(clientRequest, successExecution).block()

    // Then
    assertThat(summary.totalAmount()).isEqualTo(1.0)
  }

  @Test
  fun `should update histogram with value 0 when request execution failed`() {
    // Given
    val failExecution = ExchangeFunction { Mono.error<ClientResponse>(IllegalArgumentException("An error occured !")) }
    val clientRequest = ClientRequest.create(HttpMethod.GET, URI("http://url")).build()

    // When
    val err = assertThrows<IllegalArgumentException> {
      toTest.filter(
          clientRequest,
          failExecution
      ).block()
    }

    // Then
    assertThat(err).isNotNull()
    assertThat(summary.totalAmount()).isEqualTo(0.0)
  }

  @Test
  fun `should update histogram with value 0 when response status is Bad gateway`() {
    // Given
    val failExecution = ExchangeFunction { Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build()) }
    val clientRequest = ClientRequest.create(HttpMethod.GET, URI("http://url")).build()

    // When
    toTest.filter(clientRequest, failExecution).block()

    // Then
    assertThat(summary.totalAmount()).isEqualTo(0.0)
  }
}