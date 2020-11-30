package com.prez.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerPreferencesProfileResponse {

  String id;
  String customerId;

  SeatPreference seatPreference;
  Integer classPreference;
  String profileName;
  Locale language;
  
  public static CustomerPreferencesProfileResponse of(CustomerPreferences profile) {
    return CustomerPreferencesProfileResponse.builder()
        .id(profile.getId())
        .customerId(profile.getCustomerId())
        .seatPreference(profile.getSeatPreference())
        .classPreference(profile.getClassPreference())
        .profileName(profile.getProfileName())
        .language(profile.getLanguage())
        .build();
  } 
}