package com.prez.lib.tracing;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import brave.SpanCustomizer;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Let you customize your span to have a more friendly and readable name. It is particularly helpful in a reactive app
 */
public final class SpanCustomizationApiFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
  private final SpanCustomizer spanCustomizer;
  private final String serviceName;

  public SpanCustomizationApiFilter(SpanCustomizer spanCustomizer) {
    this(spanCustomizer, null);
  }

  public SpanCustomizationApiFilter(SpanCustomizer spanCustomizer, String serviceName) {
    this.spanCustomizer = spanCustomizer;
    this.serviceName = serviceName;
  }

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    // Pretend we're doing something meaningful ^^
    String spanName = isNotBlank(serviceName) ? serviceName : request.methodName() + " " + request.path();
    return next
        .handle(request)
        .doOnSuccess(response -> {
          spanCustomizer.name(spanName);
          spanCustomizer.tag("service", spanName);
          if (response != null) {
              SpanCustomization.tagHttpStatus(spanCustomizer, response.statusCode());
          }
        })
        .doOnError(err -> {
          spanCustomizer.name(spanName);
          spanCustomizer.tag("service", spanName);
          SpanCustomization.tagError(spanCustomizer, err);
        });
  }
}