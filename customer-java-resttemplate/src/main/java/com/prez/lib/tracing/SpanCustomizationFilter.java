package com.prez.lib.tracing;

import brave.SpanCustomizer;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@WebFilter
@Component
public class SpanCustomizationFilter implements Filter {

  private final SpanCustomizer spanCustomizer;

  public SpanCustomizationFilter(SpanCustomizer spanCustomizer) {
    this.spanCustomizer = spanCustomizer;
  }


  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    spanCustomizer.tag("http.method", request.getMethod());
    spanCustomizer.tag("http.path", request.getRequestURI());
    filterChain.doFilter(request, response);
    spanCustomizer.tag("http.statusCode", String.valueOf(response.getStatus()));
  }
}
