package com.prez.lib.tracing;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;

import brave.handler.FinishedSpanHandler;
import brave.handler.MutableSpan;
import brave.propagation.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Let you perform last minute customization on finished span before sending to zipkin
 * /!\ For this class to be called on a span, the given span must be "sampled".
 */
public class AuditSpanHandler extends FinishedSpanHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger("AUDIT_LOGGER");

  @Override
  public boolean handle(TraceContext context, MutableSpan span) {

    // we rename the spans
    String serviceName = getServiceName(span);
    LOGGER.trace("Span sampled : {},{}-{}", serviceName, context.spanIdString(), context.spanId());
    span.name(serviceName);
    // Let's say we want to filter redis spans
    return !"async".equalsIgnoreCase(span.name());
  }


  private String getServiceName(MutableSpan span) {
    if (isNotBlank(span.tag("service"))) {
      return span.tag("service");
    }
    if (isNoneBlank(span.tag("http.method"), span.tag("http.path"))) {
      return span.tag("http.method") + " " + span.tag("http.path");
    }
    return span.name();
  }

}
