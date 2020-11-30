package com.prez.api.dto;

import lombok.Value;

@Value
public class ErrorResponse {

  private String code;
  private String message;
}
