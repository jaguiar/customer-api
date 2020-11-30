package com.prez.exception;

public class NotFoundException extends RuntimeException {

  private final String id;
  private final String elementName;

  public NotFoundException(String id, String elementName) {
    this.id = id;
    this.elementName = elementName;
  }

  @Override
  public String getMessage() {
    return getLocalizedMessage();
  }

  @Override
  public String getLocalizedMessage() {
    return "No result for the given "+ elementName + " id="+ id;
  }
}
