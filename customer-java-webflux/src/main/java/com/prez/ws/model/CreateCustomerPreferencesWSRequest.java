package com.prez.ws.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateCustomerPreferencesWSRequest {

  @JsonProperty("seatPreference")
  private String seatPreference;
  @JsonProperty("classPreference")
  private Integer classPreference;
  @JsonProperty("profileName")
  private String profileName;

  public CreateCustomerPreferencesWSRequest(String seatPreference, Integer classPreference, String profileName) {
    this.seatPreference = seatPreference;
    this.classPreference = classPreference;
    this.profileName = profileName;
  }

}
