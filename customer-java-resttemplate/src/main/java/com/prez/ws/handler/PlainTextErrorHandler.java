package com.prez.ws.handler;

import com.prez.ws.WebServiceException;
import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseErrorHandler;

public class PlainTextErrorHandler implements ResponseErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextErrorHandler.class);

  private final HttpMessageConverterExtractor<String> extractor;
  private final String webServiceName;

  public PlainTextErrorHandler(String webServiceName) {
    this.webServiceName = webServiceName;
    this.extractor =
        new HttpMessageConverterExtractor<>(String.class, Collections.singletonList(new StringHttpMessageConverter()));
  }

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return !response.getStatusCode().is2xxSuccessful();
  }

  @Override
  public void handleError(@NonNull ClientHttpResponse response) throws IOException {
    String body = extractor.extractData(response);
    LOGGER.error("An error occurred while calling web service {} : body={}, statusText={}", webServiceName, body,
        response.getStatusText());
    throw new WebServiceException(webServiceName, response.getStatusCode(),
        "Unexpected error occurred when calling web service " + webServiceName);
  }
}
