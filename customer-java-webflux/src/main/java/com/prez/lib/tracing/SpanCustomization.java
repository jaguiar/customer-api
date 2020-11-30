package com.prez.lib.tracing;

import brave.SpanCustomizer;
import com.prez.ws.WebServiceException;
import org.springframework.http.HttpStatus;

public enum SpanCustomization {
  ;

  public static void tagError(final SpanCustomizer span, final Throwable e) {
    if (span == null || e == null) {
      return;
    }
    if (e instanceof WebServiceException) {
      WebServiceException pe = (WebServiceException) e;
      final String errorCode = String.valueOf(pe.getHttpStatusCode().value());
      span.tag("webService", pe.getWebServiceName());
      span.tag("http.statusCode", errorCode);
      span.tag("error", errorCode);
    } else {
      span.tag("error", e.getClass().getName());
    }
  }

  public static void tagHttpStatus(final SpanCustomizer span, final HttpStatus status) {
    if (span == null || status == null) {
      return;
    }
    span.tag("http.statusCode", String.valueOf(status.value()));
  }
}
