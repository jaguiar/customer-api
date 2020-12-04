package com.prez.config

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.PATCH
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class ResourceServerConfig {

  companion object {
    const val CUSTOMERS_URL_PATTERN = "/customers/**"
    const val SCOPE_CUSTOMER_READ = "SCOPE_customer.read"
    const val SCOPE_CUSTOMER_WRITE = "SCOPE_customer.write"
  }

  @Bean
  internal fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    http
      .authorizeExchange { exchanges ->
        exchanges
          .pathMatchers(GET, "/customers", CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_READ)
          .pathMatchers(PATCH, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
          .pathMatchers(POST, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
          .pathMatchers(PUT, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
          .pathMatchers(DELETE, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
          .pathMatchers("/app.*", "/app.health/**").permitAll() /* FOR DEMO ONLY, DO NOT DO THAT AT HOME! */
          .anyExchange()
          .authenticated()
      }
      .oauth2ResourceServer {
        it.jwt()
      }
    return http.build()
  }
}