package com.prez.lib.tracing

import brave.SpanCustomizer
import com.prez.ws.WebServiceException
import org.springframework.http.HttpStatus

/**
 * Customize spans by adding error info, HTTP status, etc.
 */
object SpanCustomization {

    fun tagError(span: SpanCustomizer?, t: Throwable?) {
        if (span == null || t == null) {
            return
        }
        if (t is WebServiceException) {
            val errorCode = t.httpStatusCode.value().toString()
            span.tag("webService", t.webServiceName)
            span.tag("http.statusCode", errorCode)
            span.tag("error", errorCode)
        } else {
            span.tag("error", t.javaClass.name)
        }
    }

    fun tagHttpStatus(span: SpanCustomizer?, status: HttpStatus?) {
        if (span == null || status == null) {
            return
        }
        span.tag("http.statusCode", status.value().toString())
    }
}
