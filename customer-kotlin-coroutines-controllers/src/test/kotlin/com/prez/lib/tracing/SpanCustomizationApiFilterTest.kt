package com.prez.lib.tracing

import brave.SpanCustomizer
import com.prez.ws.WebServiceException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URISyntaxException


@ExtendWith(MockitoExtension::class)
internal class SpanCustomizationApiFilterTest {
  @Mock
  private lateinit var mockSpan: SpanCustomizer

  private lateinit var toTest: SpanCustomizationApiFilter

  @BeforeEach
  fun init() {
    toTest = SpanCustomizationApiFilter(mockSpan, "service")
  }

  @Test
  fun `filter should tag span with http status code and error info when partner exception thrown`() {
    // Given
    val throwPartnerException = HandlerFunction {
      Mono.error(
        WebServiceException(
          webServiceName = "fancyWebService",
          httpStatusCode = HttpStatus.SERVICE_UNAVAILABLE,
          errorDescription = "Yes, as surprising as it can be partner do fail!"
        )
      )
    }
    val serverRequest = MockServerRequest.builder().build()

    // When
    val partnerException = assertThrows(
      WebServiceException::class.java
    ) { toTest.filter(serverRequest, throwPartnerException).block() }

    // Then
    org.assertj.core.api.Assertions.assertThat(partnerException).isNotNull
    verify(mockSpan).tag("service", "service")
    verify(mockSpan).tag("http.statusCode", "503")
    verify(mockSpan).tag("error", "503")
  }

  @Test
  fun `filter should tag span error info when random exception thrown`() {
    // Given
    val failExecution = HandlerFunction { Mono.error(NullPointerException("Yet another random exception")) }
    val serverRequest = MockServerRequest.builder().build()

    // When
    val partnerException = assertThrows(NullPointerException::class.java) {
      toTest.filter(serverRequest, failExecution).block()
    }

    // Then
    org.assertj.core.api.Assertions.assertThat(partnerException).isNotNull
    verify(mockSpan).tag("service", "service")
    verify(mockSpan).tag("error", "java.lang.NullPointerException")
  }

  @Test
  fun `filter should tag span with http status code when no error`() {
    // Given
    val successExecution = HandlerFunction { ServerResponse.ok().build() }
    val serverRequest = MockServerRequest.builder().build()

    // When
    toTest.filter(serverRequest, successExecution).block()

    // Then
    verify(mockSpan).tag("service", "service")
    verify(mockSpan).tag("http.statusCode", "200")
  }

  @Test
  @Throws(URISyntaxException::class)
  fun `filter should compute service name whenn none provided`() {
    // Given
    val successExecution = HandlerFunction { ServerResponse.ok().build() }
    val serverRequest = MockServerRequest.builder()
      .uri(URI("http://localhost:1234/a/random/service"))
      .method(HttpMethod.DELETE)
      .build()
    toTest = SpanCustomizationApiFilter(mockSpan)

    // When
    toTest.filter(serverRequest, successExecution).block()

    // Then
    verify(mockSpan).tag("service", "DELETE /a/random/service")
    verify(mockSpan).tag("http.statusCode", "200")
  }
}