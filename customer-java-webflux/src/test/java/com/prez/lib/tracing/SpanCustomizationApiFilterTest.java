package com.prez.lib.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import brave.SpanCustomizer;
import com.prez.ws.WebServiceException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class SpanCustomizationApiFilterTest {
  @Mock
  private SpanCustomizer mockSpan;

  private SpanCustomizationApiFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new SpanCustomizationApiFilter(mockSpan, "service");
  }

  @Test
  void filter_shouldTagSpanWithHttpStatusCodeAndErrorInfo_whenPartnerExceptionThrown() {
    // Given
    final HandlerFunction<ServerResponse> throwPartnerException =
        r -> Mono.error(new WebServiceException("fancyWebService", SERVICE_UNAVAILABLE,
            "Yes, as surprising as it can be partner do fail!"));
    final MockServerRequest serverRequest = MockServerRequest.builder().build();


    // When
    final WebServiceException partnerException = assertThrows(WebServiceException.class,
        () -> toTest.filter(serverRequest, throwPartnerException).block());

    // Then
    assertThat(partnerException).isNotNull();
    verify(mockSpan).tag("service", "service");
    verify(mockSpan).tag("http.statusCode", "503");
    verify(mockSpan).tag("error", "503");
  }

  @Test
  void filter_shouldTagSpanErrorInfo_whenRandomExceptionThrown() {
    // Given
    final HandlerFunction<ServerResponse> failExecution =
        r -> Mono.error(new NullPointerException("Yet another random exception"));
    final MockServerRequest serverRequest = MockServerRequest.builder().build();

    // When
    final NullPointerException partnerException = assertThrows(NullPointerException.class,
        () -> toTest.filter(serverRequest, failExecution).block());

    // Then
    assertThat(partnerException).isNotNull();
    verify(mockSpan).tag("service", "service");
    verify(mockSpan).tag("error", "java.lang.NullPointerException");
  }

  @Test
  void filter_shouldTagSpanWithHttpStatusCode_whenNoError() {
    // Given
    final HandlerFunction<ServerResponse> successExecution = r -> ServerResponse.ok().build();
    final MockServerRequest serverRequest = MockServerRequest.builder().build();

    // When
    toTest.filter(serverRequest, successExecution).block();

    // Then
    verify(mockSpan).tag("service", "service");
    verify(mockSpan).tag("http.statusCode", "200");
  }

  @Test
  void filter_shouldComputeServiceName_whenNotProvided() throws URISyntaxException {
    // Given
    final HandlerFunction<ServerResponse> successExecution = r -> ServerResponse.ok().build();
    final MockServerRequest serverRequest = MockServerRequest.builder()
        .uri(new URI("http://localhost:1234/a/random/service"))
        .method(HttpMethod.DELETE)
        .build();

    toTest = new SpanCustomizationApiFilter(mockSpan);

    // When
    toTest.filter(serverRequest, successExecution).block();

    // Then
    verify(mockSpan).tag("service", "DELETE /a/random/service");
    verify(mockSpan).tag("http.statusCode", "200");
  }
}