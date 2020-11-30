package com.prez.api

import com.prez.ws.WebServiceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.HandlerFunction
import reactor.core.publisher.Mono
import kotlin.test.assertFailsWith

internal class WebServiceExceptionHandlerFilterTest {

  val toTest = WebServiceExceptionHandlerFilter()

  @Test
  fun `filter should return WebServiceException HttpStatus when WebServiceException`() {
    // Given
    val webServiceException = HandlerFunction {
      Mono.error(
        WebServiceException(
          webServiceName = "shakyService", httpStatusCode = SERVICE_UNAVAILABLE,
          errorDescription = "Yes, as surprising as it can be webservices do fail!"
        )
      )
    }

    val serverRequest = MockServerRequest.builder().build();

    // When
    val serverResponse = toTest.filter(serverRequest, webServiceException).block();

    // Then
    assertThat(serverResponse.statusCode()).isEqualTo(SERVICE_UNAVAILABLE);
  }

  @Test
  fun `should propagate exceptions that are not WebServiceException`() {
    // Given
    val otherException = HandlerFunction {
      Mono.error(IllegalArgumentException("Totally random exception"))
    }

    val serverRequest = MockServerRequest.builder().build();

    // When
    val exception = assertFailsWith<IllegalArgumentException> { /* behind is a runCatching{} */
      toTest.filter(serverRequest, otherException).block()
    }

    // Then
    assertThat(exception).hasMessage("Totally random exception")
  }

}