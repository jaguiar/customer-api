package com.prez.service;

import static com.prez.model.LoyaltyStatus.B0B0B0;
import static com.prez.model.LoyaltyStatus.DBD4E0_B38BB3;
import static com.prez.model.LoyaltyStatus.E0E0E0;
import static com.prez.model.PassType.YOUTH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.PassType;
import com.prez.model.RailPass;
import com.prez.ws.model.Card;
import com.prez.ws.model.Cards;
import com.prez.ws.model.Cell;
import com.prez.ws.model.Email;
import com.prez.ws.model.File;
import com.prez.ws.model.GetCustomerWSResponse;
import com.prez.ws.model.Misc;
import com.prez.ws.model.NestedValue;
import com.prez.ws.model.PersonalDetails;
import com.prez.ws.model.PersonalInformation;
import com.prez.ws.model.Photos;
import com.prez.ws.model.Record;
import com.prez.ws.model.Service;
import com.prez.ws.model.Services;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerWSResponseToGetCustomerMapperTest {

  private CustomerWSResponseToCustomerMapper toTest = new CustomerWSResponseToCustomerMapper();

  @Test
  @DisplayName("should map if personalInformation is missing")
  void should_map_if_personalInformation_is_missing() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("my-id")
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(asList(
            Card.builder().number("card1").type(NestedValue.builder().value("WEIRD_VALUE").build()).ticketless(true)
                .disableStatus(NestedValue.builder().value("000").build()).build(),
            Card.builder().number("card2").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                .disableStatus(NestedValue.builder().value("000").build()).build(),
            Card.builder().number("card3").type(NestedValue.builder().value("FAMILY").build()).ticketless(true)
                .disableStatus(NestedValue.builder().value("000").build()).build()
        )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("LOYALTID")
                        .type(NestedValue.builder().value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "B0B0B0")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "delusional")
                            .put("loyalty_number", "LOYALTID")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "PLATINIUM")
                            .put("validity_start", "2019-11-10")
                            .put("validity_end", "2020-11-09")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "FAMILY PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "FAMILY")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass Famille")
                            .put("pass_is_active", "000").build())).build(),
                    Record.builder().otherId("29090113600311527").type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2019-12-23")
                            .put("pass_validity_start", "2018-12-23")
                            .put("pass_number", "29090113600311527")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2020-12-23")
                            .put("pass_label", "FAMILY PASS")
                            .put("some_reference", "ZZWWEE")
                            .put("new_product_code", "FAMILY")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass Famille")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("my-id")
            .firstName(null)
            .lastName(null)
            .birthDate(null)
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("LOYALTID")
                .status(B0B0B0)
                .statusRefLabel("PLATINIUM")
                .validityStartDate(LocalDate.of(2019, 11, 10))
                .validityEndDate(LocalDate.of(2020, 11, 9))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(PassType.FAMILY)
                    .typeRefLabel("FAMILY PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build(),
                RailPass.builder()
                    .number("29090113600311527")
                    .type(PassType.FAMILY)
                    .typeRefLabel("FAMILY PASS")
                    .validityStartDate(LocalDate.of(2018, 12, 23))
                    .validityEndDate(LocalDate.of(2019, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map if personalDetails is missing")
  void should_map_if_PersonalDetails_is_missing() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 3, 22))
            .alive(true)
            .build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "delusional")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 3, 22))
            .email(null)
            .phoneNumber(null)
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map when misc is missing")
  void should_map_when_misc_is_missing() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(null)
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(null)
            .build());
  }

  @Test
  @DisplayName("should map when there are several loyalty programs in misc list (one active and one inactive)")
  void should_map_when_there_are_several_loyalty_program_in_misc_list_one_active_and_one_inactive() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("149").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("149").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "E0E0E0")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "cool")
                            .put("loyalty_number", "149")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "SILVER")
                            .put("validity_start", "2016-03-31")
                            .put("validity_end", "2017-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "111").build())).build(),
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map when there is no loyalty in misc")
  void should_map_when_there_is_no_loyalty_in_misc() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("Should map and take the first one when there are several active loyalty programs in misc")
  void should_map_and_take_the_first_one_when_there_are_several_active_loyalty_in_misc() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("149").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("149").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "E0E0E0")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "cool")
                            .put("loyalty_number", "149")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "SILVER")
                            .put("validity_start", "2016-03-31")
                            .put("validity_end", "2021-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build(),
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("149")
                .status(E0E0E0)
                .statusRefLabel("SILVER")
                .validityStartDate(LocalDate.of(2016, 3, 31))
                .validityEndDate(LocalDate.of(2021, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should not map a loyalty program if number is not set in WSresponse")
  void should_not_map_a_loyalty_program_if_number_is_not_set_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(2).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should not map a loyalty program if loyalty status is not set in WSresponse")
  void should_not_map_a_loyalty_program_if_loyalty_status_is_not_set_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should not map a loyalty program if loyalty status is not a loyalty one in WSresponse")
  void should_not_map_a_loyalty_program_if_loyalty_status_is_not_a_loyalty_one_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "Mouahahahaha >:)")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should not map a loyalty program if disable status is not set in WSresponse")
  void should_not_map_a_loyalty_program_if_disableStatus_is_not_set_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(null)
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a loyalty program if validity end date is not set in WSresponse")
  void should_map_a_loyalty_program_if_validityEndDate_is_not_set_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 03, 31))
                .validityEndDate(null)
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a loyalty program if validity end date is not a date in WSresponse")
  void should_map_a_loyalty_program_if_validityEndDate_is_not_a_date_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "Mouhahaha >:)")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(null)
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a loyalty program if validity start is not set in WSresponse")
  void should_map_a_loyalty_program_if_validityStartDate_is_not_set_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "Hohohoho")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(null)
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(null)
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a loyalty program if validity start is not a date in WSresponse")
  void should_map_a_loyalty_program_if_validityStartDate_is_not_a_date_in_WSResponse() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "hohoho")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(null)
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  // Rail passes

  @Test
  @DisplayName("should map when there is no rail pass in misc")
  void should_map_when_there_is_no_rail_passes_in_misc() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(emptyList())
            .build());
  }

  @Test
  @DisplayName("should not map a rail pass if number is not set in WSresponse")
  void should_not_map_a_rail_pass_if_number_is_not_set_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder()
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(emptyList())
            .build());
  }

  @Test
  @DisplayName("should not map a rail pass if pass type is not set in WSresponse")
  void should_not_map_a_rail_pass_if_pass_type_is_not_set_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("some_reference", "UWVDJW")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(emptyList())
            .build());
  }

  @Test
  @DisplayName("should not map a rail pass if pass type is not a pass one in WSresponse")
  void should_not_map_a_rail_pass_if_pass_type_is_not_a_pass_one_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("BULBIZARRE").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "BULBIZARRE")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(emptyList())
            .build());
  }

  @Test
  @DisplayName("should not map a rail pass if disable status is not set in WSresponse")
  void should_not_map_a_rail_pass_if_disableStatus_is_not_set_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(emptyList())
            .build());
  }

  @Test
  @DisplayName("should map a rail pass if validity end date is not set in WSresponse")
  void should_map_a_rail_pass_if_validityEndDate_is_not_set_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(null)
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a rail pass if validity end date is not a date in WSresponse")
  void should_map_a_rail_pass_if_validityEndDate_is_not_a_date_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "is it a date?")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(null)
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a rail pass if validity start date is not set in WSresponse")
  void should_map_a_rail_pass_if_validityStartDate_is_not_set_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(null)
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map a rail pass if validity start date is not a date in WSresponse")
  void should_map_a_rail_pass_if_validityStartDate_is_not_a_date_in_WSResponse() {
// Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "#NotADate")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(null)
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }

  @Test
  @DisplayName("should map all")
  void should_map_all() {
    // Prepare
    final GetCustomerWSResponse getCustomerWSResponse = GetCustomerWSResponse.builder()
        .id("pikachu")
        .personalInformation(PersonalInformation.builder()
            .civility(NestedValue.builder().value("M").build())
            .lastName("Ketchum")
            .firstName("Ash")
            .birthdate(LocalDate.of(1997, 5, 22))
            .alive(true)
            .build())
        .personalDetails(PersonalDetails.builder()
            .email(
                Email.builder().address("mail@mail.com")._default(true).confirmed(NestedValue.builder().value("CHK").build())
                    .build())
            .cell(Cell.builder().number("06-07-08-09-10").build()).build())
        .cards(Cards.builder().cards(
            asList(
                Card.builder().number("150").type(NestedValue.builder().value("LOYALTY").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build(),
                Card.builder().number("card3").type(NestedValue.builder().value("YOUTH").build()).ticketless(true)
                    .disableStatus(NestedValue.builder().value("000").build()).build()
            )).build())
        .services(Services.builder().list(asList(
            Service.builder().name(NestedValue.builder().value("if-I-cannot").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:26:31Z").build(),
            Service.builder().name(NestedValue.builder().value("fix-it").build())
                .status(NestedValue.builder().value("B0B0B0").build()).updatedTime("2019-11-10T00:00:00Z").build(),
            Service.builder().name(NestedValue.builder().value("it-is").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:09Z").build(),
            Service.builder().name(NestedValue.builder().value("not-broken").build())
                .status(NestedValue.builder().value("subscribed").build()).updatedTime("2019-08-29T15:28:06Z").build()
        )).build())
        .photos(Photos.builder().file(File.builder()
            .id("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")
            .build()).build())
        .misc(asList(
            Misc.builder().type(NestedValue.builder().value("LOYALTY").build()).count(1).hasMore(true)
                .records(asList(
                    Record.builder().otherId("150").type(NestedValue.builder()
                        .value("LOYALTY").build())
                        .map(Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("loyalty_status", "DBD4E0_B38BB3")
                            .put("some_key", "some_value")
                            .put("how_are_you_today", "okay")
                            .put("loyalty_number", "150")
                            .put("old_product_code", "loyaltyELITE")
                            .put("loyalty_status_label", "MEW_TWO")
                            .put("validity_start", "2019-03-31")
                            .put("validity_end", "2025-03-31")
                            .put("status_d", "2019-11-10")
                            .put("disable_status", "000").build())).build()
                )).build(),
            Misc.builder().type(NestedValue.builder().value("PASS").build()).count(1).hasMore(false)
                .records(asList(
                    Record.builder().otherId("PID")
                        .type(NestedValue.builder().value("PASS").build()).map(
                        Maps.newHashMap(ImmutableMap.<String, String>builder()
                            .put("pass_validity_end", "2021-12-23")
                            .put("pass_validity_start", "2019-12-23")
                            .put("pass_number", "PID")
                            .put("sous_type", "PASS_QUI_S_ACHETE")
                            .put("some_date_key", "2021-12-23")
                            .put("pass_label", "YOUTH PASS")
                            .put("some_reference", "UWVDJW")
                            .put("new_product_code", "YOUTH")
                            .put("some_other_key", "for_no_reason")
                            .put("old_pass_label", "Pass pour les djeuns")
                            .put("pass_is_active", "000").build())).build()
                )).build()
        ))
        .build();

    // Test
    final Customer actual = toTest.toCustomer(getCustomerWSResponse);

    // Assert
    assertThat(actual)
        .isNotNull()
        .usingRecursiveComparison().isEqualTo(
        Customer.builder()
            .customerId("pikachu")
            .firstName("Ash")
            .lastName("Ketchum")
            .birthDate(LocalDate.of(1997, 5, 22))
            .email("mail@mail.com")
            .phoneNumber("06-07-08-09-10")
            .loyaltyProgram(LoyaltyProgram.builder()
                .number("150")
                .status(DBD4E0_B38BB3)
                .statusRefLabel("MEW_TWO")
                .validityStartDate(LocalDate.of(2019, 3, 31))
                .validityEndDate(LocalDate.of(2025, 3, 31))
                .build())
            .railPasses(asList(
                RailPass.builder()
                    .number("PID")
                    .type(YOUTH)
                    .typeRefLabel("YOUTH PASS")
                    .validityStartDate(LocalDate.of(2019, 12, 23))
                    .validityEndDate(LocalDate.of(2021, 12, 23))
                    .build()
            ))
            .build());
  }
}