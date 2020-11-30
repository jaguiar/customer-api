package com.prez.lib.tracing

import brave.SpanCustomizer
import com.prez.ws.WebServiceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono
import java.net.URI


@ExtendWith(MockitoExtension::class)
internal class SpanCustomizationWebClientFilterTest {
  @Mock
  private lateinit var mockSpan: SpanCustomizer

  private lateinit var toTest: SpanCustomizationWebClientFilter

  @BeforeEach
  fun init() {
    toTest = SpanCustomizationWebClientFilter("fancyService", mockSpan)
  }

  @Test
  @Throws(Exception::class)
  fun `filter should tag span with http status code and error info when partner exception thrown`() {
    // Given
    val partnerFail = ExchangeFunction {
      Mono.error(
          WebServiceException(
              webServiceName = "fancyService", httpStatusCode = SERVICE_UNAVAILABLE,
              errorDescription = "Yes, as surprising as it can be web services do fail!"
          )
      )
    }
    val clientRequest = ClientRequest.create(GET, URI("http://url")).build()

    // When
    val partnerException = assertThrows(WebServiceException::class.java) {
      toTest.filter(clientRequest, partnerFail).block()
    }

    // Then
    assertThat(partnerException).isNotNull
    Mockito.verify(mockSpan, Mockito.times(2)).tag("webService", "fancyService")
    Mockito.verify(mockSpan).tag("http.statusCode", "503")
    Mockito.verify(mockSpan).tag("error", "503")
  }

  @Test
  @Throws(Exception::class)
  fun `filter should tag span error info when random exception thrown`() {
    // Given
    val failExchange = ExchangeFunction { Mono.error(NullPointerException("Yet another random exception")) }
    val clientRequest = ClientRequest.create(GET, URI("http://url")).build()

    // When
    val partnerException = assertThrows(NullPointerException::class.java) {
      toTest.filter(clientRequest, failExchange).block()
    }

    // Then
    assertThat(partnerException).isNotNull
    Mockito.verify(mockSpan).tag("webService", "fancyService")
    Mockito.verify(mockSpan).tag("error", "java.lang.NullPointerException")
  }

  @Test
  @Throws(Exception::class)
  fun `filter should tag span with http status code when no error`() {
    // Given
    val successExchange = ExchangeFunction { Mono.just(ClientResponse.create(HttpStatus.CREATED).build()) }
    val clientRequest = ClientRequest.create(GET, URI("http://url")).build()

    // When
    toTest.filter(clientRequest, successExchange).block()

    // Then
    Mockito.verify(mockSpan).tag("webService", "fancyService")
    Mockito.verify(mockSpan).tag("http.statusCode", "201")
  }
}