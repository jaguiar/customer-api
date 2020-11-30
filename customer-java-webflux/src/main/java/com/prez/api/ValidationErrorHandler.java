package com.prez.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.reactive.function.server.ServerResponse.status;

import com.prez.api.dto.ErrorResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class ValidationErrorHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ValidationErrorHandler.class);

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return next
        .handle(request)
        .onErrorResume(e -> {
              LOGGER.error(
                  Optional.ofNullable(e.getCause()).map(Throwable::getLocalizedMessage).orElse(e.getLocalizedMessage()),
                  e);
              return status(BAD_REQUEST).bodyValue(new ErrorResponse("VALIDATION_ERROR", "Bad input"));
            }
        );
  }
}
