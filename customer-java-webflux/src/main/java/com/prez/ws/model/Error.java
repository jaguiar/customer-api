package com.prez.ws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Error implements Serializable {

  @JsonProperty("error")
  private String error;
  @JsonProperty("error_description")
  private String errorDescription;
}
