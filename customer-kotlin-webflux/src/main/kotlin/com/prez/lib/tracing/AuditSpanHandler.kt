package com.prez.lib.tracing

import org.apache.commons.lang3.StringUtils.isNoneBlank
import org.apache.commons.lang3.StringUtils.isNotBlank

import brave.handler.FinishedSpanHandler
import brave.handler.MutableSpan
import brave.propagation.TraceContext
import org.slf4j.LoggerFactory

/**
 * Let you perform last minute customization on finished span before sending to zipkin
 * /!\ For this class to be called on a span, the given span must be "sampled".
 */
class AuditSpanHandler : FinishedSpanHandler() {

    companion object {
        private val logger = LoggerFactory.getLogger("AUDIT_LOGGER")
    }

    override fun handle(context: TraceContext, span: MutableSpan): Boolean {
        // we rename the spans
        val serviceName = getServiceName(span)
        logger.trace("Span sampled : {},{}-{}", serviceName, context.spanIdString(), context.spanId())
        span.name(serviceName)
        // Let's say we want to filter redis spans
        return !"async".equals(span.name(), ignoreCase = true)
    }

    private fun getServiceName(span: MutableSpan) = when {
        isNotBlank(span.tag("service")) -> span.tag("service")
        isNoneBlank(
            span.tag("http.method"),
            span.tag("http.path")
        ) -> "${span.tag("http.method")} ${span.tag("http.path")}"
        else -> span.name()
    }
}
