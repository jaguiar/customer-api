package com.prez.api;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

import com.prez.api.dto.ErrorResponse;
import com.prez.exception.NotFoundException;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ElementNotFoundErrorHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {
  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return next
        .handle(request)
        .onErrorResume(NotFoundException.class, ex ->
            status(NOT_FOUND)
                .bodyValue(new ErrorResponse("NOT_FOUND", ex.getLocalizedMessage()))
        );
  }
}
