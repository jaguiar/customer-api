package com.prez.api

import com.prez.api.dto.ErrorResponse
import com.prez.ws.WebServiceException
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.json
import reactor.core.publisher.Mono

/**
 * Let you handle web service exception
 */
class WebServiceExceptionHandlerFilter : HandlerFilterFunction<ServerResponse, ServerResponse> {

  override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
    return next
      .handle(request)
      .onErrorResume(WebServiceException::class.java) { ex ->
        status(ex.httpStatusCode)
          .json()
          .bodyValue(ErrorResponse(ex.error.error, ex.error.errorDescription))
      }
  }
}