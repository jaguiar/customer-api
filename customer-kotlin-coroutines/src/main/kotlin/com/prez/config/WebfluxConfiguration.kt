package com.prez.config

import brave.SpanCustomizer
import com.prez.api.CreateCustomerPreferencesHandler
import com.prez.api.CustomJwtTokenHandler
import com.prez.api.ElementNotFoundErrorHandler
import com.prez.api.GetCustomerHandler
import com.prez.api.GetCustomerPreferencesHandler
import com.prez.api.GlobalErrorHandler
import com.prez.api.ValidationErrorHandler
import com.prez.api.WebServiceExceptionHandlerFilter
import com.prez.lib.tracing.SpanCustomizationApiFilter
import kotlinx.coroutines.FlowPreview
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class WebfluxConfiguration(val spanCustomizer: SpanCustomizer) {

  // This is necessary to allow bean validation
  @Bean
  @Primary
  internal fun springValidator(): Validator = LocalValidatorFactoryBean()

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

  @Bean
  internal fun getCustomer(getCustomerHandler: GetCustomerHandler) = coRouter {
    (GET("/customers") and (accept(APPLICATION_JSON)))
      .invoke(getCustomerHandler::getCustomer)
  }.filter(SpanCustomizationApiFilter(spanCustomizer, "GET /customers"))
    .filter(CustomJwtTokenHandler())
    .filter(WebServiceExceptionHandlerFilter())
    .filter(ElementNotFoundErrorHandler())
    .filter(GlobalErrorHandler())

  @Bean
  fun createCustomerPreferences(createCustomerPreferencesHandler: CreateCustomerPreferencesHandler) = coRouter {
    (POST("/customers/preferences") and (accept(APPLICATION_JSON)))
      .invoke(createCustomerPreferencesHandler::createCustomerPreferences)
  }.filter(SpanCustomizationApiFilter(spanCustomizer))
    .filter(CustomJwtTokenHandler())
    .filter(ValidationErrorHandler())
    .filter(WebServiceExceptionHandlerFilter())
    .filter(GlobalErrorHandler())

  @FlowPreview
  @Bean
  fun getCustomerPreferences(getCustomerPreferencesHandler: GetCustomerPreferencesHandler) = coRouter {
    (GET("/customers/preferences") and (accept(APPLICATION_JSON)))
      .invoke(getCustomerPreferencesHandler::getCustomerPreferences)
  }.filter(SpanCustomizationApiFilter(spanCustomizer))
    .filter(CustomJwtTokenHandler())
    .filter(WebServiceExceptionHandlerFilter())
    .filter(ElementNotFoundErrorHandler())
    .filter(GlobalErrorHandler())
}
