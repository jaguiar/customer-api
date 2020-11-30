package com.prez.config

import brave.handler.FinishedSpanHandler
import com.prez.lib.tracing.AuditSpanHandler
import com.prez.lib.tracing.CleanLoggingContextFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.web.server.WebFilter

/**
 * Auto-configuration [org.springframework.boot.autoconfigure.EnableAutoConfiguration] that adds
 *  - an audit handler to log the duration of finished spans
 *  - a [org.springframework.web.server.WebFilter] to clean logging context after each request
 */
@Configuration
class TracingConfig {

    @Bean
    @ConditionalOnMissingBean
    fun auditFinishedSpanHandler(): FinishedSpanHandler {
        return AuditSpanHandler()
    }

    @Bean
    @Order(-999)  // Keep this number low to be sure this filter is executed last
    fun cleanLoggingContextFilter(): WebFilter {
        return CleanLoggingContextFilter()
    }
}