package com.prez.config;

import static java.util.Arrays.asList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import brave.SpanCustomizer;
import com.prez.api.CreateCustomerPreferencesHandler;
import com.prez.api.CustomJwtTokenHandler;
import com.prez.api.ElementNotFoundErrorHandler;
import com.prez.api.GetCustomerHandler;
import com.prez.api.GetCustomerPreferencesHandler;
import com.prez.api.GlobalErrorHandler;
import com.prez.api.ValidationErrorHandler;
import com.prez.api.WebServiceExceptionHandlerFilter;
import com.prez.lib.tracing.SpanCustomizationApiFilter;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;


@EnableWebFlux
@Configuration
public class WebfluxConfiguration {

  private final SpanCustomizer spanCustomizer;

  public WebfluxConfiguration(SpanCustomizer spanCustomizer) {
    this.spanCustomizer = spanCustomizer;
  }

  // This is necessary to allow bean validation
  @Bean
  @Primary
  public Validator springValidator() {
    return new LocalValidatorFactoryBean();
  }

  @Order(Ordered.HIGHEST_PRECEDENCE)
  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration corsConfig = new CorsConfiguration();
    corsConfig.setAllowedOrigins(Collections.singletonList("*"));
    corsConfig.setMaxAge(3600L);
    corsConfig.setAllowedMethods(asList("PUT", "OPTIONS", "GET", "POST"));
    corsConfig.applyPermitDefaultValues(); // to allow all headers, be more selective at home of course

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfig);

    return new CorsWebFilter(source);
  }

  /* For the resttemplate version the routes are declared in the controller and controller method annotations */
  @Bean
  public RouterFunction<ServerResponse> getCustomer(final GetCustomerHandler getCustomerHandler) {
    return route(GET("/customers")
        .and(accept(APPLICATION_JSON)), getCustomerHandler::getCustomer)
        .filter(new SpanCustomizationApiFilter(spanCustomizer, "GET /customers"))
        .filter(new CustomJwtTokenHandler())
        .filter(new WebServiceExceptionHandlerFilter())
        .filter(new ElementNotFoundErrorHandler())
        .filter(new GlobalErrorHandler());
  }

  @Bean
  public RouterFunction<ServerResponse> createCustomerPreferences(
      final CreateCustomerPreferencesHandler createCustomerPreferencesHandler) {
    return route(POST("/customers/preferences")
        .and(accept(APPLICATION_JSON)), createCustomerPreferencesHandler::handleRequest)
        .filter(new SpanCustomizationApiFilter(spanCustomizer))
        .filter(new CustomJwtTokenHandler())
        .filter(new ValidationErrorHandler())
        .filter(new WebServiceExceptionHandlerFilter())
        .filter(new GlobalErrorHandler());
  }

  @Bean
  public RouterFunction<ServerResponse> getCustomerPreferences(
      final GetCustomerPreferencesHandler getCustomerPreferencesHandler) {
    return route(GET("/customers/preferences")
        .and(accept(APPLICATION_JSON)), getCustomerPreferencesHandler::getCustomerPreferences)
        .filter(new SpanCustomizationApiFilter(spanCustomizer))
        .filter(new CustomJwtTokenHandler())
        .filter(new WebServiceExceptionHandlerFilter())
        .filter(new ElementNotFoundErrorHandler())
        .filter(new GlobalErrorHandler());
  }


}
