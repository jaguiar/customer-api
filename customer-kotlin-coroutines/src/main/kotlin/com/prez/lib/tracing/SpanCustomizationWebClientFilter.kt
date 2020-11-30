package com.prez.lib.tracing

import brave.SpanCustomizer
import com.prez.lib.tracing.SpanCustomization.tagError
import com.prez.lib.tracing.SpanCustomization.tagHttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

/**
 * Filtre permettant de customiser les spans applicables à un appel via le webClient.
 * Notamment, permet d'attacher au span le nom du partenaire appelé, le statut HTTP de retour et
 * éventuellement le message d'erreur le cas échéant
 */
class SpanCustomizationWebClientFilter(private val webServiceName: String, private val spanCustomizer: SpanCustomizer) : ExchangeFilterFunction {

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        return next.exchange(request)
                .doOnError {
                    spanCustomizer.tag("webService", webServiceName)
                    tagError(spanCustomizer, it)
                }
                .doOnSuccess {
                    spanCustomizer.tag("webService", webServiceName)
                    tagHttpStatus(spanCustomizer, it.statusCode())
                }
    }
}

