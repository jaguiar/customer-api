package com.prez.ws.handler;

import brave.SpanCustomizer;
import com.prez.lib.tracing.SpanCustomization;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Just a decorator of a RestTemplate {@link ResponseErrorHandler} to demonstrate span customization.
 */
public class SpanCustomizationWebClientErrorHandler implements ResponseErrorHandler {

  private final SpanCustomizer spanCustomizer;
  private final String webServiceName;
  private final ResponseErrorHandler errorHandler;

  public SpanCustomizationWebClientErrorHandler(String webServiceName, ResponseErrorHandler errorHandler, SpanCustomizer spanCustomizer) {
    this.spanCustomizer = spanCustomizer;
    this.webServiceName = webServiceName;
    this.errorHandler = errorHandler;
  }

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return errorHandler.hasError(response);
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    try {
      spanCustomizer.tag("webService", webServiceName);
      errorHandler.handleError(response);
      SpanCustomization.tagHttpStatus(spanCustomizer, response.getStatusCode());
    } catch (Exception e) {
      SpanCustomization.tagError(spanCustomizer, e); // correlation
      throw e;
    }
  }

  public ResponseErrorHandler getWrappedErrorHandler() {
    return errorHandler;
  }
}
