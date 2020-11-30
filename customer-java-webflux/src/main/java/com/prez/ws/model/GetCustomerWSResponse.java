package com.prez.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
public class GetCustomerWSResponse implements Serializable {

  @JsonProperty("id")
  private String id;
  @JsonProperty("personalInformation")
  private PersonalInformation personalInformation;
  @JsonProperty("personalDetails")
  private PersonalDetails personalDetails;
  @JsonProperty("cards")
  private Cards cards;
  @JsonProperty("services")
  private Services services;
  @JsonProperty("photos")
  private Photos photos;
  @JsonProperty("misc")
  private List<Misc> misc = new ArrayList<>();

}
