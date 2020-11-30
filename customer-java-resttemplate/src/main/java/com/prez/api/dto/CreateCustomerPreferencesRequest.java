package com.prez.api.dto;

import com.prez.model.SeatPreference;
import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateCustomerPreferencesRequest implements Serializable {

  @NotNull(message = "{NotNull.seatPreference}")
  private SeatPreference seatPreference;
  @NotNull(message = "{NotNull.classPreference}")
  @Min(value = 1, message = "{Min.classPreference}")
  @Max(value = 2, message = "{Max.classPreference}")
  private Integer classPreference;
  @NotNull(message = "{NotNull.profileName}")
  @Size(min = 1, max = 50, message = "{Size.profileName}")
  @Pattern(regexp = "(\\p{IsAlphabetic}|\\s|-)*", message = "{Pattern.profileName}")
  private String profileName;
  @Pattern(message = "{NotValid.language}", regexp = "(:?fr|de|es|en|it|pt)")
  private String language;
}
