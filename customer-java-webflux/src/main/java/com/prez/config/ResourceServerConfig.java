package com.prez.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class ResourceServerConfig {

  public static final String CUSTOMERS_URL_PATTERN = "/customers/**";
  public static final String SCOPE_CUSTOMER_READ = "SCOPE_customer.read";
  public static final String SCOPE_CUSTOMER_WRITE = "SCOPE_customer.write";

  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http
        .authorizeExchange(exchanges ->
            exchanges
                .pathMatchers(GET, "/customers", CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_READ)
                .pathMatchers(PATCH, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
                .pathMatchers(POST, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
                .pathMatchers(PUT, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
                .pathMatchers(DELETE, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
                .pathMatchers("/app.*", "/app.health/**").permitAll() /* FOR DEMO ONLY, DO NOT DO THAT AT HOME! */
                .pathMatchers("/favicon.ico").permitAll()
                .anyExchange()
                .authenticated()
        )
        .oauth2ResourceServer(
            ServerHttpSecurity.OAuth2ResourceServerSpec::jwt
        );
    return http.build();
  }

}
