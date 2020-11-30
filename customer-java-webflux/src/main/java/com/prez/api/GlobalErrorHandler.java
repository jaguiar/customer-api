package com.prez.api;

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

public class GlobalErrorHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandler.class);

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return next
        .handle(request)
        .onErrorResume(e -> {
              LOGGER.error(
                  Optional.ofNullable(e.getCause()).map(Throwable::getLocalizedMessage).orElse(e.getLocalizedMessage()),
                  e);
              return status(INTERNAL_SERVER_ERROR).bodyValue(new ErrorResponse("UNEXPECTED_ERROR",
                  "Something horribly wrong happened, I could tell you what but then I’d have to kill you."));
            }
        );
  }
}
