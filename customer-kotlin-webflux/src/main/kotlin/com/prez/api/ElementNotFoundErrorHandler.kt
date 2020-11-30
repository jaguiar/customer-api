package com.prez.api

import com.prez.api.dto.ErrorResponse
import com.prez.exception.NotFoundException
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


class ElementNotFoundErrorHandler : HandlerFilterFunction<ServerResponse, ServerResponse> {
  override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
    return next
        .handle(request)
        .onErrorResume(NotFoundException::class.java) { ex: NotFoundException ->
          ServerResponse.status(NOT_FOUND)
              .bodyValue(ErrorResponse("NOT_FOUND", ex.localizedMessage))
        }
  }
}