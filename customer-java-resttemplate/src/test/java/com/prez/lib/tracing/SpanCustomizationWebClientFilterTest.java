package com.prez.lib.tracing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import brave.SpanCustomizer;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class SpanCustomizationWebClientFilterTest {

  private final SpanCustomizer spanCustomizer = mock(SpanCustomizer.class);

  private SpanCustomizationWebClientFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new SpanCustomizationWebClientFilter("MyFunkyPartner", spanCustomizer);
  }

  @Test
  void intercept_shouldTagSpanWithPartnerName() throws IOException {
    // Given
    MockClientHttpRequest mockRequest = new MockClientHttpRequest();
    final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
    final byte[] body = {};
    when(execution.execute(mockRequest, body)).thenReturn(new MockClientHttpResponse(new byte[] {}, HttpStatus.CREATED));

    // When
    toTest.intercept(mockRequest, body, execution);

    // Then
    verify(spanCustomizer).tag("webService", "MyFunkyPartner");
    verify(spanCustomizer).tag("http.statusCode", "201");
  }

}