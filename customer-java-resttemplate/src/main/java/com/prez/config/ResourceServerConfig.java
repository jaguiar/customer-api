package com.prez.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

  public static final String CUSTOMERS_URL_PATTERN = "/customers/**";
  public static final String SCOPE_CUSTOMER_READ = "SCOPE_customer.read";
  public static final String SCOPE_CUSTOMER_WRITE = "SCOPE_customer.write";

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .mvcMatcher(CUSTOMERS_URL_PATTERN)
        .authorizeRequests()
        .mvcMatchers(GET, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_READ)
        .mvcMatchers(PATCH, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
        .mvcMatchers(POST, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
        .mvcMatchers(PUT, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
        .mvcMatchers(DELETE, CUSTOMERS_URL_PATTERN).hasAuthority(SCOPE_CUSTOMER_WRITE)
        .and()
        .oauth2ResourceServer()
        .jwt();
  }

}
