package com.prez.api

import com.prez.api.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class GlobalErrorHandler : HandlerFilterFunction<ServerResponse, ServerResponse> {

  private val logger = LoggerFactory.getLogger(GlobalErrorHandler::class.java)

  override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
    return next
      .handle(request)
      .onErrorResume { err ->
        logger.error(err.cause?.localizedMessage ?: err.localizedMessage, err)
        ServerResponse.status(INTERNAL_SERVER_ERROR)
          .bodyValue(ErrorResponse("UNEXPECTED_ERROR",
            "Something horribly wrong happened, I could tell you what but then I’d have to kill you."))
      }
  }
}