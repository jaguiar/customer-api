package com.prez.api

import com.prez.api.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

class ValidationErrorHandler : HandlerFilterFunction<ServerResponse, ServerResponse> {
  private val logger = LoggerFactory.getLogger(ValidationErrorHandler::class.java)
  override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
    return next
      .handle(request)
      .onErrorResume({ e -> e is ServerWebInputException || e is DecodingException }) { e ->
        logger.error(e.cause?.localizedMessage ?: e.localizedMessage, e)
        ServerResponse.status(BAD_REQUEST)
          .bodyValue(ErrorResponse("VALIDATION_ERROR", e.localizedMessage ?: "Bad input"))
      }
  }
}