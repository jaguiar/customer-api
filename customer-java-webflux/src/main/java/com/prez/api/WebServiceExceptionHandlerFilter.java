package com.prez.api;

import static org.springframework.web.reactive.function.server.ServerResponse.status;

import com.prez.api.dto.ErrorResponse;
import com.prez.ws.WebServiceException;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Let you handle web service exception
 */
public final class WebServiceExceptionHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return next
        .handle(request)
        .onErrorResume(WebServiceException.class, ex ->
            status(ex.getHttpStatusCode())
                .bodyValue(new ErrorResponse(ex.getError().getError(), ex.getError().getErrorDescription()))
        );
  }
}