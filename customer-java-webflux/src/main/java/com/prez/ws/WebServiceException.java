package com.prez.ws;

import com.prez.ws.model.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WebServiceException extends RuntimeException {

  private static final String GENERIC_ERROR_NAME = "WEBSERVICE_ERROR";
  private final String webServiceName;
  private final HttpStatus httpStatusCode;
  private Error error;

  public WebServiceException(String webServiceName, HttpStatus statusCode, String errorDescription) {
    this(GENERIC_ERROR_NAME, webServiceName, statusCode, errorDescription);
  }

  public WebServiceException(String errorName, String webServiceName, HttpStatus statusCode, String errorDescription) {
    super(errorName + " : webService=" + webServiceName + ", statusCode=" + statusCode + " : " + errorDescription);
    this.webServiceName = webServiceName;
    this.error = new Error(errorName, errorDescription);
    this.httpStatusCode = statusCode;
  }
}
