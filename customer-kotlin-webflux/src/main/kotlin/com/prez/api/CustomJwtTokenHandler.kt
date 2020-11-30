package com.prez.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

class CustomJwtTokenHandler : HandlerFilterFunction<ServerResponse, ServerResponse> {

  private val logger = LoggerFactory.getLogger(CustomJwtTokenHandler::class.java)

  override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
    return request.principal()
      .doOnSuccess {
        logger.debug("Principal type : {} - value : {}", it.javaClass.name, it)
      }
      .ofType(JwtAuthenticationToken::class.java)
      .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "No principal")))
      // Log just to see if we get the value of the custom azp attribute, but imagine we are doing something meaningful here
      .doOnSuccess {
        logger.info("Principal info : name={}, azp={}",
          it.name,
          it.tokenAttributes["azp"]
        )

      }.then(next.handle(request))
  }
}