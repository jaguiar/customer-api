package com.prez.api;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class CustomJwtTokenHandler implements HandlerFilterFunction<ServerResponse, ServerResponse> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomJwtTokenHandler.class);

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return request.principal()
        .doOnSuccess(principal
            -> LOGGER.debug("Principal type : {} - value : {}", principal.getClass().getName(), principal))
        .ofType(JwtAuthenticationToken.class)
        .switchIfEmpty(Mono.error(new ResponseStatusException(UNAUTHORIZED, "No principal")))
        // Log just to see if we get the value of the custom azp attribute, but imagine we are doing something meaningful here
        .doOnSuccess(principal ->
            LOGGER.info("Principal info : name={}, azp={}",
                principal.getName(),
                principal.getTokenAttributes().get("azp")))
        .then(next.handle(request));
  }
}