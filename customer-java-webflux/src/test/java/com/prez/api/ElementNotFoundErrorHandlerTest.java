package com.prez.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.prez.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class ElementNotFoundErrorHandlerTest {
  private ElementNotFoundErrorHandler toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new ElementNotFoundErrorHandler();
  }

  @Test
  @DisplayName("filter should return 404 HttpStatus when NotFoundException")
  void filter_shouldReturn404HttpStatus_whenNotFoundException() {
    // Given
    final HandlerFunction<ServerResponse> webServiceException =
        r -> Mono.error(new NotFoundException("Nonexistent", "Being"));

    final MockServerRequest serverRequest = MockServerRequest.builder().build();

    // When
    final ServerResponse serverResponse = toTest.filter(serverRequest, webServiceException).block();

    // Then
    assertThat(serverResponse.statusCode()).isEqualTo(NOT_FOUND);
    // FIXME we are not able to test the body
  }

  @Test
  @DisplayName("should propagate exceptions that are not NotFoundException")
  void filter_shouldPropagateException_whenOtherException() {
    // Given
    final HandlerFunction<ServerResponse> otherException =
        r -> Mono.error(new IllegalArgumentException("Totally random exception"));

    final MockServerRequest serverRequest = MockServerRequest.builder().build();

    // When
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> toTest.filter(serverRequest, otherException).block());

    // Then
    assertThat(exception)
        .isNotNull()
        .hasMessage("Totally random exception");
  }
}