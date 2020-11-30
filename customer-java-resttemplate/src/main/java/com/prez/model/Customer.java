package com.prez.model;

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "customer")
@Data
@Builder
@NoArgsConstructor(force = true, access = PRIVATE)
@AllArgsConstructor
public class Customer implements Serializable {
  @NotBlank
  @NonFinal
  @Id
  private String customerId;
  private String lastName;
  private String firstName;
  private LocalDate birthDate;
  private String phoneNumber;
  private String email;
  private LoyaltyProgram loyaltyProgram;
  private List<RailPass> railPasses = new ArrayList<>();
}
