package com.prez.lib.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import brave.SpanCustomizer;
import com.prez.ws.WebServiceException;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
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
class SpanCustomizationWebClientFilterTest {

  @Mock
  private SpanCustomizer mockSpan;

  private SpanCustomizationWebClientFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new SpanCustomizationWebClientFilter("fancyService", mockSpan);
  }

  @Test
  void filter_shouldTagSpanWithHttpStatusCodeAndErrorInfo_whenPartnerExceptionThrown() throws Exception {
    // Given
    final ExchangeFunction partnerFail = r -> Mono.error(new WebServiceException("fancyService", SERVICE_UNAVAILABLE,
        "Yes, as surprising as it can be web services do fail!"));
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    final WebServiceException partnerException = assertThrows(WebServiceException.class,
        () -> toTest.filter(clientRequest, partnerFail).block());

    // Then
    assertThat(partnerException).isNotNull();
    verify(mockSpan, times(2)).tag("webService", "fancyService");
    verify(mockSpan).tag("http.statusCode", "503");
    verify(mockSpan).tag("error", "503");
  }

  @Test
  void filter_shouldTagSpanErrorInfo_whenRandomExceptionThrown() throws Exception {
    // Given
    final ExchangeFunction failExchange = r -> Mono.error(new NullPointerException("Yet another random exception"));
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    final NullPointerException partnerException = assertThrows(NullPointerException.class,
        () -> toTest.filter(clientRequest, failExchange).block());

    // Then
    assertThat(partnerException).isNotNull();
    verify(mockSpan).tag("webService", "fancyService");
    verify(mockSpan).tag("error", "java.lang.NullPointerException");
  }

  @Test
  void filter_shouldTagSpanWithHttpStatusCode_whenNoError() throws Exception {
    // Given
    final ExchangeFunction successExchange = r -> Mono.just(ClientResponse.create(HttpStatus.CREATED).build());
    final ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, new URI("http://url")).build();

    // When
    toTest.filter(clientRequest, successExchange).block();

    // Then
    verify(mockSpan).tag("webService", "fancyService");
    verify(mockSpan).tag("http.statusCode", "201");
  }
}