package com.prez.lib.tracing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import brave.SpanCustomizer;
import java.io.IOException;
import javax.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SpanCustomizationFilterTest {

  private final SpanCustomizer spanCustomizer = mock(SpanCustomizer.class);
  private SpanCustomizationFilter toTest;

  @BeforeEach
  void beforeEach() {
    toTest = new SpanCustomizationFilter(spanCustomizer);
  }

  @Test
  void doFilter_shouldTagSpanWithHttpInfo() throws IOException, ServletException {
    // Given
    final MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    servletRequest.setMethod("PUT");
    servletRequest.setRequestURI("/winter/is/gone");

    final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
    servletResponse.setStatus(403);

    // When
    toTest.doFilter(servletRequest, servletResponse, new MockFilterChain());

    // Then
    verify(spanCustomizer).tag("http.method", "PUT");
    verify(spanCustomizer).tag("http.path", "/winter/is/gone");
    verify(spanCustomizer).tag("http.statusCode", "403");

  }

}