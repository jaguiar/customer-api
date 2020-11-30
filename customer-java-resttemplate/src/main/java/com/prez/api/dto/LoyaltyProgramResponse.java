package com.prez.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class LoyaltyProgramResponse implements Serializable {

  private String number;
  private String label;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate validityStartDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private LocalDate validityEndDate;
}
