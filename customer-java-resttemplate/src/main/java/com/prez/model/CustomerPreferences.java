package com.prez.model;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Locale;

@Value
@With
@Builder
@ToString
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode(of = {"customerId"})
@Document(collection = "preferences")
public class CustomerPreferences implements Serializable {

  @Id
  String id;
  @Indexed
  String customerId;

  SeatPreference seatPreference;
  Integer classPreference;
  @Indexed
  String profileName;
  Locale language;

    public CustomerPreferences copy() {
        return CustomerPreferences.builder()
                .id(this.getId())
                .customerId(this.getCustomerId())
                .seatPreference(this.getSeatPreference())
                .classPreference(this.getClassPreference())
                .profileName(this.getProfileName())
                .language(this.language)
                .build();
    }
}
