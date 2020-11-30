package com.prez.lib.tracing;

import brave.SpanCustomizer;
import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

/**
 * Filtre permettant de personaliser les spans applicables à un appel via le resttemplate.
 * Notamment, permet d'attacher au span le nom du partenaire appelé, le statut HTTP de retour et
 * éventuellement le message d'erreur le cas échéant
 */
public class SpanCustomizationWebClientFilter implements ClientHttpRequestInterceptor {

  private final String webServiceName;
  private final SpanCustomizer spanCustomizer;

  public SpanCustomizationWebClientFilter(final String webServiceName, SpanCustomizer spanCustomizer) {
    this.webServiceName = webServiceName;
    this.spanCustomizer = spanCustomizer;
  }

  @Override
  public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
                                      @NonNull ClientHttpRequestExecution execution) throws IOException {
    spanCustomizer.tag("webService", webServiceName);
    final ClientHttpResponse response = execution.execute(request, body);
    SpanCustomization.tagHttpStatus(spanCustomizer, response.getStatusCode());
    return response;
  }
}
