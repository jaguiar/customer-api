package com.prez.ws.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
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
public class PersonalInformation implements Serializable {

  @JsonProperty("civility")
  private NestedValue civility;
  @JsonProperty("lastName")
  private String lastName;
  @JsonProperty("firstName")
  private String firstName;
  @JsonProperty("birthdate")
  private LocalDate birthdate;
  @JsonProperty("alive")
  private boolean alive;

}
