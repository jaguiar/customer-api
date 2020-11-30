package com.prez.lib.tracing;

import brave.SpanCustomizer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * Filtre permettant de personaliser les spans applicables à un appel via le webClient.
 * Notamment, permet d'attacher au span le nom du partenaire appelé, le statut HTTP de retour et
 * éventuellement le message d'erreur le cas échéant
 */
public class SpanCustomizationWebClientFilter implements ExchangeFilterFunction {

  private final String webServiceName;
  private final SpanCustomizer spanCustomizer;

  public SpanCustomizationWebClientFilter(String webServiceName, SpanCustomizer spanCustomizer) {
    this.webServiceName = webServiceName;
    this.spanCustomizer = spanCustomizer;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return next
        .exchange(request)
        .doOnSuccess(response -> {
          spanCustomizer.tag("webService", webServiceName);
          SpanCustomization.tagHttpStatus(spanCustomizer, response.statusCode());
        })
        .doOnError(err -> {
          spanCustomizer.tag("webService", webServiceName);
          SpanCustomization.tagError(spanCustomizer, err);
        });
  }
}
