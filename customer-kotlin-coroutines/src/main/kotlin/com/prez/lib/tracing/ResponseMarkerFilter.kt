package com.prez.lib.tracing

import io.micrometer.core.instrument.DistributionSummary
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

class ResponseMarkerFilter(private val webServiceName: String, private val summary: DistributionSummary) : ExchangeFilterFunction {

    companion object {
        private val logger = LoggerFactory.getLogger(ResponseMarkerFilter::class.java)
    }

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {
        return next.exchange(request)
                .doOnSuccess { resp ->
                    val result = if (resp.statusCode().is5xxServerError) 0.0 else 1.0
                    summary.record(result)
                    logger.debug("Call to webService {} was {}, for request : {} {}", webServiceName,
                            if (result == 1.0) "OK" else "KO", request.method(), request.url().path)
                }
                .doOnError {
                    summary.record(0.0)
                    logger.error("Call to webservice {} was KO, for request : {} {}", webServiceName, request.method(), request.url().path)
                }
    }
}
