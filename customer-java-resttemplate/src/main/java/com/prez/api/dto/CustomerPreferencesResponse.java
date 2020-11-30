package com.prez.api.dto;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prez.model.CustomerPreferences;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerPreferencesResponse {
  List<CustomerPreferencesProfileResponse> profiles;

  public static CustomerPreferencesResponse of(List<CustomerPreferences> customerPreferences) {
    List<CustomerPreferencesProfileResponse> profiles = customerPreferences.stream()
        .map(profiled -> CustomerPreferencesProfileResponse.builder()
            .id(profiled.getId())
            .customerId(profiled.getCustomerId())
            .seatPreference(profiled.getSeatPreference())
            .classPreference(profiled.getClassPreference())
            .profileName(profiled.getProfileName())
            .language(profiled.getLanguage())
            .build()).collect(toList());
    return CustomerPreferencesResponse.builder().profiles(profiles).build();
  }
}
