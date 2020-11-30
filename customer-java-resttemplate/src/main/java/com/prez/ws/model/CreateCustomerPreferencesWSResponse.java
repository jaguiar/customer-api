package com.prez.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCustomerPreferencesWSResponse implements Serializable {

  @JsonProperty("id")
  private String id;
  @JsonProperty("seatPreference")
  private String seatPreference;
  @JsonProperty("classPreference")
  private Integer classPreference;
  @JsonProperty("profileName")
  private String profileName;

}
