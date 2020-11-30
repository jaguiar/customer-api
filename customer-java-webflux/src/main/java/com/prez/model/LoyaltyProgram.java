package com.prez.model;

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor
public class LoyaltyProgram implements Serializable {

  private String number;
  private LoyaltyStatus status;
  private String statusRefLabel;
  private LocalDate validityStartDate;
  private LocalDate validityEndDate;
}
