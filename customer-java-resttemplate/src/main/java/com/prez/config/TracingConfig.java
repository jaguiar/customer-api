package com.prez.config;

import brave.handler.SpanHandler;
import com.prez.lib.tracing.AuditSpanHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration [org.springframework.boot.autoconfigure.EnableAutoConfiguration] that adds
 * - an audit api to log the duration of finished spans
 * - a [org.springframework.web.server.WebFilter] to clean logging context after each request
 */
@Configuration
public class TracingConfig {

  @Bean
  @ConditionalOnMissingBean
  SpanHandler auditFinishedSpanHandler() {
    return new AuditSpanHandler();
  }

  /* CleanLoggingContextFilter is annotated so no need to declare here */
}
