package com.prez.extension

import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus.B0B0B0
import com.prez.model.LoyaltyStatus.E0E0E0
import com.prez.model.LoyaltyStatus.DBD4E0_B38BB3
import com.prez.model.PassType
import com.prez.model.PassType.YOUTH
import com.prez.model.RailPass
import com.prez.ws.model.Card
import com.prez.ws.model.Cards
import com.prez.ws.model.Cell
import com.prez.ws.model.Email
import com.prez.ws.model.File
import com.prez.ws.model.GetCustomerWSResponse
import com.prez.ws.model.Misc
import com.prez.ws.model.NestedValue
import com.prez.ws.model.PersonalDetails
import com.prez.ws.model.PersonalInformation
import com.prez.ws.model.Photos
import com.prez.ws.model.Record
import com.prez.ws.model.Service
import com.prez.ws.model.Services
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CustomerWSModelExtensionTest {

  @Test
  fun `should map if personalInformation is missing`() {
    // Prepare
    val customerModel = GetCustomerWSResponse(
      id = "my-id",
      personalInformation = null,
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell(number = "06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "card1", type = NestedValue("WEIRD_VALUE"),
            ticketless = true, disableStatus = NestedValue("000")
          ),
          Card(
            number = "card2", type = NestedValue("LOYALTY"),
            ticketless = true, disableStatus = NestedValue("000")
          ),
          Card(
            number = "card1", type = NestedValue("FAMILY"),
            ticketless = true, disableStatus = NestedValue("000")
          ),
        )
      ),
      services = Services(
        listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"),
            updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File(id = "http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),

      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "LOYALTID",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "B0B0B0"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "delusional"),
                mapOf("key" to "loyalty_number", "value" to "LOYALTID"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "PLATINIUM"),
                mapOf("key" to "validity_start", "value" to "2019-11-10"),
                mapOf("key" to "validity_end", "value" to "2020-11-09"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 2, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "FAMILY PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "FAMILY"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass Famille"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            ),
            Record(
              otherId = "29090113600311527",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2019-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2018-12-23"),
                mapOf("key" to "pass_number", "value" to "29090113600311527"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2020-12-23"),
                mapOf("key" to "pass_label", "value" to "FAMILY PASS"),
                mapOf("key" to "some_reference", "value" to "ZZWWEE"),
                mapOf("key" to "new_product_code", "value" to "FAMILY"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass Famille"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = customerModel.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "my-id",
          firstName = null,
          lastName = null,
          birthDate = null,
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "LOYALTID",
            status = B0B0B0,
            statusRefLabel = "PLATINIUM",
            validityStartDate = LocalDate.of(2019, 11, 10),
            validityEndDate = LocalDate.of(2020, 11, 9)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = PassType.FAMILY,
              typeRefLabel = "FAMILY PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23)
            ),
            RailPass(
              number = "29090113600311527",
              type = PassType.FAMILY,
              typeRefLabel = "FAMILY PASS",
              validityStartDate = LocalDate.of(2018, 12, 23),
              validityEndDate = LocalDate.of(2019, 12, 23)
            )
          )
        )
      )
  }

  @Test
  fun `should map if personalDetails is missing`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-03-22",
        alive = true
      ),
      personalDetails = null,
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "delusional"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 3, 22),
          email = null,
          phoneNumber = null,
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should map when misc is missing`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf()
        )
      )
  }

  @Test
  fun `should map when there are several loyalty programs in misc list (one active and one inactive)`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "149",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "E0E0E0"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "cool"),
                mapOf("key" to "loyalty_number", "value" to "149"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "SILVER"),
                mapOf("key" to "validity_start", "value" to "2016-03-31"),
                mapOf("key" to "validity_end", "value" to "2017-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "111")
              )
            ),
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should map when there is no loyalty in misc`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should map and take the first one when there are several active loyalty programs in misc`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "149",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "E0E0E0"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "cool"),
                mapOf("key" to "loyalty_number", "value" to "149"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "SILVER"),
                mapOf("key" to "validity_start", "value" to "2016-03-31"),
                mapOf("key" to "validity_end", "value" to "2021-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )),
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )))
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )))
        ))
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "149",
            status = E0E0E0,
            statusRefLabel = "SILVER",
            validityStartDate = LocalDate.of(2016, 3, 31),
            validityEndDate = LocalDate.of(2021, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should not map a loyalty program if number is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(number = null, type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = null,
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should not map a loyalty program if loyalty status is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should not map a loyalty program if loyalty status is not a loyalty one in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "Mouahahahaha >:)"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should not map a loyalty program if disable status is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = null
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = null,
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should map a loyalty program if validity end date is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            ))
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = null
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should map a loyalty program if validity end date is not a date in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "Mouhahaha >:)"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = null
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  @Test
  fun `should a loyalty program if validity start is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = null,
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  fun `should map a loyalty program if validity start is not a date in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "hohoho"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = null,
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }

  // Rail passes

  @Test
  fun `should map when there is no rail pass in misc`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf()
          )
      )
  }

  @Test
  fun `should not map a rail pass if number is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = null, type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf()
        )
      )
  }

  @Test
  fun `should not map a rail pass if pass type is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = null, ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf()
        )
      )
  }

  @Test
  fun `should not map a rail pass if pass type is not a pass one in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("BULBIZARRE"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "BULBIZARRE"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf()
        )
      )
  }

  @Test
  fun `should not map a rail pass if disable status is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = null
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf()
        )
      )
  }

  @Test
  fun `should map a rail pass if validity end date is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = null
            )
          )
        )
      )
  }

  @Test
  fun `should map a rail pass if validity end date is not a date in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "is it a date?"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = null
            )
          )
        )
      )
  }

  @Test
  fun `should map a rail pass if validity start date is not set in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = null,
              validityEndDate = LocalDate.of(2021, 12, 23)
            )
          )
        )
      )
  }

  @Test
  fun `should map a rail pass if validity start date is not a date in WSresponse`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "#NotADate"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = null,
              validityEndDate = LocalDate.of(2021, 12, 23)
            )
          )
        )
      )
  }

  @Test
  fun `should map all`() {
    // Prepare
    val toTest = GetCustomerWSResponse(
      id = "pikachu",
      personalInformation = PersonalInformation(
        civility = NestedValue("M"),
        lastName = "Ketchum",
        firstName = "Ash",
        birthdate = "1997-05-22",
        alive = true
      ),
      personalDetails = PersonalDetails(
        Email("mail@mail.com", true, NestedValue("CHK")),
        Cell("06-07-08-09-10")
      ),
      cards = Cards(
        listOf(
          Card(
            number = "150", type = NestedValue("LOYALTY"), ticketless = true,
            disableStatus = NestedValue("000")
          ),
          Card(
            number = "card3", type = NestedValue("YOUTH"), ticketless = true,
            disableStatus = NestedValue("000")
          )
        )
      ),
      services = Services(
        list = listOf(
          Service(
            name = NestedValue("if-I-cannot"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:26:31Z"
          ),
          Service(
            name = NestedValue("fix-it"),
            status = NestedValue("B0B0B0"), updatedTime = "2019-11-10T00:00:00Z"
          ),
          Service(
            name = NestedValue("it-is"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:09Z"
          ),
          Service(
            name = NestedValue("not-broken"),
            status = NestedValue("subscribed"), updatedTime = "2019-08-29T15:28:06Z"
          )
        )
      ),
      photos = Photos(File("http://localhost:8080/castlemock/web/rest/Project/fsHJCG/application/f5tXVc/resource/kLUscw/my-id/photos/file")),
      misc = listOf(
        Misc(
          type = NestedValue("LOYALTY"), count = 1, hasMore = true,
          records = listOf(
            Record(
              otherId = "150",
              type = NestedValue("LOYALTY"),
              map = listOf(
                mapOf("key" to "loyalty_status", "value" to "DBD4E0_B38BB3"),
                mapOf("key" to "some_key", "value" to "some_value"),
                mapOf("key" to "how_are_you_today", "value" to "okay"),
                mapOf("key" to "loyalty_number", "value" to "150"),
                mapOf("key" to "old_product_code", "value" to "loyaltyELITE"),
                mapOf("key" to "loyalty_status_label", "value" to "MEW_TWO"),
                mapOf("key" to "validity_start", "value" to "2019-03-31"),
                mapOf("key" to "validity_end", "value" to "2025-03-31"),
                mapOf("key" to "status_d", "value" to "2019-11-10"),
                mapOf("key" to "disable_status", "value" to "000")
              )
            )
          )
        ),
        Misc(
          type = NestedValue("PASS"), count = 1, hasMore = false,
          records = listOf(
            Record(
              otherId = "PID",
              type = NestedValue("PASS"),
              map = listOf(
                mapOf("key" to "pass_validity_end", "value" to "2021-12-23"),
                mapOf("key" to "pass_validity_start", "value" to "2019-12-23"),
                mapOf("key" to "pass_number", "value" to "PID"),
                mapOf("key" to "sous_type", "value" to "PASS_QUI_S_ACHETE"),
                mapOf("key" to "some_date_key", "value" to "2021-12-23"),
                mapOf("key" to "pass_label", "value" to "YOUTH PASS"),
                mapOf("key" to "some_reference", "value" to "UWVDJW"),
                mapOf("key" to "new_product_code", "value" to "YOUTH"),
                mapOf("key" to "some_other_key", "value" to "for_no_reason"),
                mapOf("key" to "old_pass_label", "value" to "Pass pour les djeuns"),
                mapOf("key" to "pass_is_active", "value" to "000")
              )
            )
          )
        )
      )
    )

    // Test
    val actual = toTest.toCustomer()

    // Assert
    assertThat(actual).isNotNull
      .usingRecursiveComparison().isEqualTo(
        Customer(
          customerId = "pikachu",
          firstName = "Ash",
          lastName = "Ketchum",
          birthDate = LocalDate.of(1997, 5, 22),
          email = "mail@mail.com",
          phoneNumber = "06-07-08-09-10",
          loyaltyProgram = LoyaltyProgram(
            number = "150",
            status = DBD4E0_B38BB3,
            statusRefLabel = "MEW_TWO",
            validityStartDate = LocalDate.of(2019, 3, 31),
            validityEndDate = LocalDate.of(2025, 3, 31)
          ),
          railPasses = listOf(
            RailPass(
              number = "PID",
              type = YOUTH,
              typeRefLabel = "YOUTH PASS",
              validityStartDate = LocalDate.of(2019, 12, 23),
              validityEndDate = LocalDate.of(2021, 12, 23),
            )
          )
        )
      )
  }
}