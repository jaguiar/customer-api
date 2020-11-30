package com.prez.ws;

import com.prez.ws.model.Error;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WebServiceException extends RuntimeException {

  private final String webServiceName;
  private final HttpStatus httpStatusCode;
  private Error error;

  public WebServiceException(String webServiceName, HttpStatus statusCode, String error) {
    super("Webservice error : webService=" + webServiceName + ", statusCode=" + statusCode + " : " + error);
    this.webServiceName = webServiceName;
    this.error = new Error(statusCode.toString(), error);
    this.httpStatusCode = statusCode;
  }

  public WebServiceException(String errorName, String webServiceName, HttpStatus statusCode, String error) {
    super(errorName + " : webService=" + webServiceName + ", statusCode=" + statusCode + " : " + error);
    this.webServiceName = webServiceName;
    this.error = new Error(statusCode.toString(), error);
    this.httpStatusCode = statusCode;
  }
}
