package com.prez.lib.tracing;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.DistributionSummary;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ResponseMarkerFilterTest {

  @Mock
  private DistributionSummary summary;

  private ResponseMarkerFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new ResponseMarkerFilter(summary);
  }

  @Test
  @DisplayName("should update summary with value 1 when request execution succeeded")
  void filter_shouldUpdateSummaryWithValue1_whenRequestExecutionSucceeded() throws URISyntaxException {
    // Given
    final ExchangeFunction successExecution = r -> Mono.just(ClientResponse.create(HttpStatus.OK).build());
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    toTest.filter(clientRequest, successExecution).block();

    // Then
    verify(summary).record(1);
  }

  @Test
  @DisplayName("should update summary with value 0 when request execution failed")
  void filter_shouldUpdateSummaryWithValue0_whenRequestExecutionFailed() throws URISyntaxException {
    // Given
    final ExchangeFunction failExecution = r -> Mono.error(new IllegalArgumentException("Something terrible happened!"));
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    final IllegalArgumentException err =
        assertThrows(IllegalArgumentException.class, () -> toTest.filter(clientRequest, failExecution).block());

    // Then
    assertThat(err).isNotNull();
    verify(summary).record(0);
  }

  @Test
  @DisplayName("should update summary with value 0 when response status is Bad gateway")
  void filter_shouldUpdateSummaryWithValue0_whenResponseStatusIsBadGateway() throws URISyntaxException {
    // Given
    final ExchangeFunction failExecution = r -> Mono.just(ClientResponse.create(HttpStatus.BAD_GATEWAY).build());
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    toTest.filter(clientRequest, failExecution).block();

    // Then
    verify(summary).record(0);
  }

}