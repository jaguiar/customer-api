package com.prez.lib.tracing;

import io.micrometer.core.instrument.DistributionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * Each time an HTTP response is received, this interceptor will put a mark in a given distribution summary.
 * If the response status code is 5xx or if an error occurred during the execution of the request,
 * the stored value will be 0, otherwise the stored value will be 1 as the web service is reachable.
 * This will be used to compute the ratio between the number of failed and successful calls to a partner, for example.
 */
public class ResponseMarkerFilter implements ExchangeFilterFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseMarkerFilter.class);

  private final DistributionSummary summary;

  public ResponseMarkerFilter(DistributionSummary summary) {
    this.summary = summary;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return next.exchange(request)
        .doOnSuccess(resp -> {
          int result = resp.statusCode().is5xxServerError() ? 0 : 1;
          summary.record(result);
          LOGGER.debug("Call to webService was {}, for request : {} {}", result == 1 ? "OK" : "KO",
              request.method(), request.url().getPath());
        })
        .doOnError(err -> {
          summary.record(0);
          LOGGER.error("Call to webService {} was KO, for request : {} {}", request.method(),
              request.url().getPath());
        });
  }
}
