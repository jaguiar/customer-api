package com.prez.ws.handler;

import com.prez.ws.WebServiceException;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class CustomerErrorHandler implements ResponseErrorHandler {

  @Override
  public boolean hasError(ClientHttpResponse response) throws IOException {
    return HttpStatus.OK != response.getStatusCode() && HttpStatus.NOT_FOUND != response.getStatusCode();
  }

  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    throw new WebServiceException("CustomerWS", response.getStatusCode(), "Unexpected response from the server.");
  }
}
