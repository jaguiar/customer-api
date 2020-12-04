package com.prez.config

import brave.SpanCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class CorsFilter(val spanCustomizer: SpanCustomizer) {

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  internal fun corsWebFilter(): CorsWebFilter {
    val corsConfig = CorsConfiguration()
    corsConfig.allowedOrigins = listOf("*")
    corsConfig.maxAge = 3600L
    corsConfig.allowedMethods = listOf("PUT", "OPTIONS", "GET", "POST")
    corsConfig.applyPermitDefaultValues() // to allow all headers, be more selective at home of course
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", corsConfig)
    return CorsWebFilter(source)
  }
}
