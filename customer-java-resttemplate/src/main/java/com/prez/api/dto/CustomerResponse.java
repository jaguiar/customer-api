package com.prez.api.dto;

import static java.time.LocalDate.now;
import static java.time.Period.between;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.RailPass;
import java.util.List;
import java.util.stream.Collectors;
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
public class CustomerResponse {

  private String customerId;
  private String firstName;
  private String lastName;
  private Integer age;
  private String phoneNumber;
  private String email;
  private LoyaltyProgramResponse loyaltyProgram;
  private List<RailPassResponse> railPasses;

  public static CustomerResponse of(Customer customer) {
    CustomerResponse.CustomerResponseBuilder builder = CustomerResponse.builder()
        .customerId(customer.getCustomerId())
        .lastName(customer.getLastName())
        .firstName(customer.getFirstName())
        .phoneNumber(customer.getPhoneNumber())
        .email(customer.getEmail());
    if (customer.getBirthDate() != null) {
      builder.age(between(customer.getBirthDate(), now()).getYears());
    }
    final LoyaltyProgram loyaltyProgram = customer.getLoyaltyProgram();
    if (loyaltyProgram != null) {
      String label = isNotEmpty(loyaltyProgram.getStatusRefLabel()) ? loyaltyProgram.getStatusRefLabel() :
          loyaltyProgram.getStatus().name();
      builder.loyaltyProgram(LoyaltyProgramResponse.builder()
          .number(loyaltyProgram.getNumber())
          .validityStartDate(loyaltyProgram.getValidityStartDate())
          .validityEndDate(loyaltyProgram.getValidityEndDate())
          .label(label).build());
    }
    final List<RailPass> railPasses = customer.getRailPasses();
    if (railPasses != null) {
      builder.railPasses(railPasses
          .stream()
          .map(railPass -> {
                String label = isNotEmpty(railPass.getTypeRefLabel()) ? railPass.getTypeRefLabel() : railPass.getType().name();
                return RailPassResponse.builder()
                    .number(railPass.getNumber())
                    .validityStartDate(railPass.getValidityStartDate())
                    .validityEndDate(railPass.getValidityEndDate())
                    .label(label).build();
              }
          )
          .collect(Collectors.toList()));
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "customerId:"+ customerId +
        ", firstName:" + replaceIfSet(firstName) +
        ", lastName:" + (replaceIfSet(lastName)) +
        ", age:" + (age != null ? "SET" : "null") +
        ", phoneNumber:" + (replaceIfSet(phoneNumber)) +
        ", email:" + (replaceIfSet(firstName)) +
        ", loyaltyProgram:" + loyaltyProgram +
        ", railPasses:[" + railPasses == null ? "" : railPasses.stream().map(RailPassResponse::toString).collect(joining(",")) + "]";
  }

  private String replaceIfSet(String s) {
    return isNotBlank(s) ? "SET" : "null";
  }
}