package com.prez.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import com.prez.ws.WebServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

class WebServiceExceptionHandlerFilterTest {
  private WebServiceExceptionHandlerFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new WebServiceExceptionHandlerFilter();
  }

  @Test
  @DisplayName("filter should return WebServiceException HttpStatus when WebServiceException")
  void filter_shouldReturnWebServiceExceptionHttpStatus_whenWebServiceException() {
    // Given
    final HandlerFunction<ServerResponse> webServiceException =
        r -> Mono.error(new WebServiceException("shakyService", SERVICE_UNAVAILABLE,
            "Yes, as surprising as it can be web services do fail!"));

    final MockServerRequest serverRequest = MockServerRequest.builder().build();

    // When
    final ServerResponse serverResponse = toTest.filter(serverRequest, webServiceException).block();

    // Then
    assertThat(serverResponse.statusCode()).isEqualTo(SERVICE_UNAVAILABLE);
  }

  @Test
  @DisplayName("should propagate exceptions that are not WebServiceException")
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