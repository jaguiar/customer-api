package com.prez.api

import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

/**
 * Since Webflux/Spring does not handle CLIENT DISCONNECTED ERROR ( to be exact, it "handles" it by returning a `200` http status code response with an empty body ),
 * this class tries to implement some logic for this case only, mostly by reproducing the behaviour of the "faulty" Spring one.
 * @see [org.springframework.web.server.adapter.HttpWebHandlerAdapter.handleUnresolvedError] line 265 to 270.
 */
@Component
@Order(-2)
class DisconnectedClientWebExceptionHandler : WebExceptionHandler {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DisconnectedClientWebExceptionHandler::class.java)
    }

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        val requestQuery = exchange.request.uri.query
        val response = exchange.response

        return when {
            response.isCommitted && isDisconnectedClientError(ex) -> {
                // After the response is committed, propagate errors to the server and light a candle...
                LOGGER.error(
                    "Error for ${requestQuery} but ServerHttpResponse already committed ( ${response.statusCode} )",
                    ex
                )
                Mono.error(ResponseStatusException(INTERNAL_SERVER_ERROR, "Unexpected error : ${ex.javaClass.name}"))
            }
            !response.isCommitted && isDisconnectedClientError(ex) -> {
                response.setStatusCode(INTERNAL_SERVER_ERROR)
                LOGGER.error("500 Server Error for ${requestQuery}", ex)
                Mono.empty()
            }
            else -> Mono.error(ex)
        }
    }

    /**
     * Completely copied from [org.springframework.web.server.adapter.HttpWebHandlerAdapter.DISCONNECTED_CLIENT_EXCEPTIONS]
     */
    // Similar declaration exists in AbstractSockJsSession..
    private val disconnectedClientExceptions =
        setOf("AbortedException", "ClientAbortException", "EOFException", "EofException")

    /**
     * Completely copied from [org.springframework.web.server.adapter.HttpWebHandlerAdapter.isDisconnectedClientError]
     */
    private fun isDisconnectedClientError(ex: Throwable): Boolean {
        val message = NestedExceptionUtils.getMostSpecificCause(ex).message?.toLowerCase().orEmpty()
        return message.contains("broken pipe")
            || message.contains("connection reset by peer")
            || (disconnectedClientExceptions.contains(ex.javaClass.simpleName))
    }
}