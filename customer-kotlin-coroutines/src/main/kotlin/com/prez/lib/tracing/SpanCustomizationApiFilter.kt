package com.prez.lib.tracing

import brave.SpanCustomizer
import com.prez.lib.tracing.SpanCustomization.tagError
import org.springframework.web.reactive.function.server.HandlerFilterFunction
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

/**
 * Let you customize your span to have a more friendly and readable name. It is particularly helpful in a reactive app
 */
class SpanCustomizationApiFilter(private val spanCustomizer: SpanCustomizer, private val serviceName: String = "") :
    HandlerFilterFunction<ServerResponse, ServerResponse> {

    override fun filter(request: ServerRequest, next: HandlerFunction<ServerResponse>): Mono<ServerResponse> {
        // Pretend we're doing something meaningful ^^
        val spanName = if (serviceName.isBlank()) "${request.methodName()} ${request.path()}" else serviceName
        return next
            .handle(request)
            .doOnSuccess { response ->
                spanCustomizer.name(spanName)
                spanCustomizer.tag("service", spanName)
                response?.let { SpanCustomization.tagHttpStatus(spanCustomizer, it.statusCode()) }
            }
            .doOnError { err ->
                spanCustomizer.name(spanName);
                spanCustomizer.tag("service", spanName);
                err?.let { tagError(spanCustomizer, it) }
            }
    }
}