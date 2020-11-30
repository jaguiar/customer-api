package com.prez.service;

import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.LoyaltyStatus;
import com.prez.model.PassType;
import com.prez.model.RailPass;
import com.prez.ws.model.GetCustomerWSResponse;
import com.prez.ws.model.Misc;
import com.prez.ws.model.PersonalDetails;
import com.prez.ws.model.PersonalInformation;
import com.prez.ws.model.Record;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomerWSResponseToCustomerMapper {


  private final static String LOYALTY_PROGRAM_TYPE = "LOYALTY";
  private final static String LOYALTY_NUMBER_FIELD_NAME = "loyalty_number";
  private final static String LOYALTY_STATUS_FIELD_NAME = "loyalty_status";
  private final static String LOYALTY_LABEL_FIELD_NAME = "loyalty_status_label";
  private final static String LOYALTY_VALIDITY_START_FIELD_NAME = "validity_start";
  private final static String LOYALTY_VALIDITY_END_FIELD_NAME = "validity_end";
  private final static String LOYALTY_DISABLE_STATUS_FIELD_NAME = "disable_status";

  private final static String RAIL_PASS_TYPE = "PASS";
  private final static String PASS_NUMBER_FIELD_NAME = "pass_number";
  private final static String PASS_PRODUCT_CODE_FIELD_NAME = "new_product_code";
  private final static String PASS_PRODUCT_LABEL_FIELD_NAME = "pass_label";
  private final static String PASS_VALIDITY_START_FIELD_NAME = "pass_validity_start";
  private final static String PASS_VALIDITY_END_FIELD_NAME = "pass_validity_end";
  private final static String PASS_ACTIVE_STATUS_FIELD_NAME = "pass_is_active";

  private final static String ACTIVE_FIELD_VALUE = "000";

  private final static Logger logger = LoggerFactory.getLogger(CustomerWSResponseToCustomerMapper.class);

  public Customer toCustomer(GetCustomerWSResponse getCustomerWSResponse) {
    Customer.CustomerBuilder builder = Customer.builder()
        .customerId(getCustomerWSResponse.getId());
    final PersonalInformation personalInformation = getCustomerWSResponse.getPersonalInformation();
    if (personalInformation != null) {
      builder.firstName(personalInformation.getFirstName())
          .lastName(personalInformation.getLastName())
          .birthDate(personalInformation.getBirthdate());
    }
    final PersonalDetails personalDetails = getCustomerWSResponse.getPersonalDetails();
    if (personalDetails != null) {
      if (personalDetails.getEmail() != null) {
        builder.email(personalDetails.getEmail().getAddress());
      }
      if (personalDetails.getCell() != null) {
        builder.phoneNumber(personalDetails.getCell().getNumber());
      }
    }

    final List<Misc> allmisc = getCustomerWSResponse.getMisc();
    if (allmisc != null) {

      // map loyalty programs
      final List<LoyaltyProgram> loyaltyPrograms = allmisc.stream()
          .filter(misc -> misc.getType() != null && LOYALTY_PROGRAM_TYPE.equals(misc.getType().getValue()))
          .map(Misc::getRecords)
          .flatMap(List::stream)
          .filter(record -> record.getType() != null && LOYALTY_PROGRAM_TYPE.equals(record.getType().getValue()) &&
              record.getMap() != null)
          .map(Record::getMap)
          // we check that we have the required fields
          .filter(allFields -> isNotBlank(allFields.get(LOYALTY_NUMBER_FIELD_NAME))
              && ACTIVE_FIELD_VALUE.equals(allFields.get(LOYALTY_DISABLE_STATUS_FIELD_NAME))
              && EnumUtils.isValidEnum(LoyaltyStatus.class, allFields.get(LOYALTY_STATUS_FIELD_NAME)))
          .map(allFields -> LoyaltyProgram.builder()
              .number(allFields.get(LOYALTY_NUMBER_FIELD_NAME))
              .status(LoyaltyStatus.valueOf(allFields.get(LOYALTY_STATUS_FIELD_NAME)))
              .statusRefLabel(allFields.get(LOYALTY_LABEL_FIELD_NAME))
              .validityStartDate(parseDateOrNull(allFields.get(LOYALTY_VALIDITY_START_FIELD_NAME)))
              .validityEndDate(parseDateOrNull(allFields.get(LOYALTY_VALIDITY_END_FIELD_NAME)))
              .build())
          .collect(toCollection(ArrayList::new));
      if (loyaltyPrograms.size() == 1) {
        builder.loyaltyProgram(loyaltyPrograms.get(0));
      } else if (loyaltyPrograms.size() > 1) { // we log something because it's weird seriously
        logger.warn("Ok there is something weird with customer id='{}', they has {} loyalty programs",
            getCustomerWSResponse.getId(), loyaltyPrograms.size());
        builder.loyaltyProgram(loyaltyPrograms.get(0)); // and we take the first because why not?
      }

      // map passes
      final List<RailPass> railPasses = allmisc.stream()
          .filter(misc -> misc.getType() != null && RAIL_PASS_TYPE.equals(misc.getType().getValue()))
          .map(Misc::getRecords)
          .flatMap(List::stream)
          .filter(record -> record.getType() != null && RAIL_PASS_TYPE.equals(record.getType().getValue()) &&
              record.getMap() != null)
          .map(Record::getMap)
          .filter(allFields -> isNotBlank(allFields.get(PASS_NUMBER_FIELD_NAME))
              && ACTIVE_FIELD_VALUE.equals(allFields.get(PASS_ACTIVE_STATUS_FIELD_NAME))
              && EnumUtils.isValidEnum(PassType.class, allFields.get(PASS_PRODUCT_CODE_FIELD_NAME)))
          .map(allFields -> RailPass.builder()
              .number(allFields.get(PASS_NUMBER_FIELD_NAME))
              .type(PassType.valueOf(allFields.get(PASS_PRODUCT_CODE_FIELD_NAME)))
              .typeRefLabel(allFields.get(PASS_PRODUCT_LABEL_FIELD_NAME))
              .validityStartDate(parseDateOrNull(allFields.get(PASS_VALIDITY_START_FIELD_NAME)))
              .validityEndDate(parseDateOrNull(allFields.get(PASS_VALIDITY_END_FIELD_NAME)))
              .build())
          .collect(toCollection(ArrayList::new));
      builder.railPasses(railPasses);
    }

    return builder.build();
  }


  private LocalDate parseDateOrNull(String maybeDate) {
    if (maybeDate != null) {
      try {
        return LocalDate.parse(maybeDate);
      } catch (DateTimeParseException e) {
      }
    }
    return null;
  }
}
