package com.prez.api

import com.prez.exception.NotFoundException
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.HandlerFunction
import reactor.core.publisher.Mono


internal class ElementNotFoundErrorHandlerTest {
  private var toTest: ElementNotFoundErrorHandler = ElementNotFoundErrorHandler()

  @Test
  fun `filter should return 404 HttpStatus when NotFoundException`() {
    // Given
    val webServiceException = HandlerFunction { Mono.error(NotFoundException("Nonexistent", "Being")) }
    val serverRequest = MockServerRequest.builder().build()

    // When
    val serverResponse = toTest.filter(serverRequest, webServiceException).block()

    // Then
    assertThat(serverResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    // FIXME we are not able to test the body
  }

  @Test
  fun `should propagate exceptions that are not NotFoundException`() {
    // Given
    val otherException = HandlerFunction { Mono.error(IllegalArgumentException("Totally random exception")) }
    val serverRequest = MockServerRequest.builder().build()

    // When
    val exception = assertThrows<IllegalArgumentException> {
      toTest.filter(serverRequest, otherException).block()
    }

    // Then
    assertThat(exception).isNotNull.hasMessage("Totally random exception")
  }
}