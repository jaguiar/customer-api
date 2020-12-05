package com.prez.extension

import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.LoyaltyStatus
import com.prez.model.PassType
import com.prez.model.RailPass
import com.prez.ws.model.GetCustomerWSResponse
import com.prez.ws.model.Record
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Extension functions ( mapper mostly ) for model objects
 */

//just for demo, may need to go somewhere else
private const val LOYALTY_PROGRAM_TYPE = "LOYALTY"
private const val LOYALTY_NUMBER_FIELD_NAME = "loyalty_number"
private const val LOYALTY_STATUS_FIELD_NAME = "loyalty_status"
private const val LOYALTY_LABEL_FIELD_NAME = "loyalty_status_label"
private const val LOYALTY_VALIDITY_START_FIELD_NAME = "validity_start"
private const val LOYALTY_VALIDITY_END_FIELD_NAME = "validity_end"
private const val LOYALTY_DISABLE_STATUS_FIELD_NAME = "disable_status"

private const val RAIL_PASS_TYPE = "PASS"
private const val PASS_NUMBER_FIELD_NAME = "pass_number"
private const val PASS_PRODUCT_CODE_FIELD_NAME = "new_product_code"
private const val PASS_PRODUCT_LABEL_FIELD_NAME = "pass_label"
private const val PASS_VALIDITY_START_FIELD_NAME = "pass_validity_start"
private const val PASS_VALIDITY_END_FIELD_NAME = "pass_validity_end"
private const val PASS_ACTIVE_STATUS_FIELD_NAME = "pass_is_active"

private const val ACTIVE_FIELD_VALUE = "000"

fun GetCustomerWSResponse.toCustomer() =
  Customer(
      customerId = id,
      email = personalDetails?.email?.address,
      firstName = personalInformation?.firstName,
      lastName = personalInformation?.lastName,
      phoneNumber = personalDetails?.cell?.number,
      birthDate = personalInformation?.birthdate?.let { LocalDate.parse(it) },
      loyaltyProgram = toLoyaltyProgram(),
      railPasses = toRailPasses()
  )

fun GetCustomerWSResponse.toLoyaltyProgram(): LoyaltyProgram? =
  misc
    .filter { LOYALTY_PROGRAM_TYPE == it.type?.value && 0 < it.count } // we filter to retrieve only the loyalty cards sublist that has at least one loyalty card
    .flatMap {
      findAndConvertLoyaltyPrograms(it.records)
    }
    .firstOrNull()  // and we take the first because why not?

private fun findAndConvertLoyaltyPrograms(allFields: List<Record>): Iterable<LoyaltyProgram> =
  allFields
    .filter { LOYALTY_PROGRAM_TYPE == it.type?.value && isActiveAndHasRequiredLoyaltyProgramFields(it.mapAsRealMap) }
    .map {
      LoyaltyProgram(
          number = it.getValue(LOYALTY_NUMBER_FIELD_NAME),
          status = LoyaltyStatus.valueOf(it.getValue(LOYALTY_STATUS_FIELD_NAME)),
          statusRefLabel = it.getMaybeValue(LOYALTY_LABEL_FIELD_NAME),

          validityStartDate = parseDateOrNull(it.getMaybeValue(LOYALTY_VALIDITY_START_FIELD_NAME)),
          validityEndDate = parseDateOrNull(it.getMaybeValue(LOYALTY_VALIDITY_END_FIELD_NAME))
      )
    }

// we check that we have at least :
// the loyalty program number
// the loyalty program status
// that the card is REALLY a loyalty program one ( because you never really know...)
// that the card is active
private fun isActiveAndHasRequiredLoyaltyProgramFields(allFields: Map<String, String>): Boolean =
  allFields.contains(LOYALTY_NUMBER_FIELD_NAME) &&
      ACTIVE_FIELD_VALUE == allFields[LOYALTY_DISABLE_STATUS_FIELD_NAME] &&
      enumContains<LoyaltyStatus>(allFields[LOYALTY_STATUS_FIELD_NAME])

fun GetCustomerWSResponse.toRailPasses(): List<RailPass> =
  misc
    .filter { allMisc -> allMisc.type?.value == RAIL_PASS_TYPE }
    .flatMap { allMisc ->
      allMisc.records
        .filter { it.type?.value == RAIL_PASS_TYPE && isActiveAndHasRequiredRailPassFields(it.mapAsRealMap) }
        .map {
          RailPass(
              number = it.getValue(PASS_NUMBER_FIELD_NAME),
              type = PassType.valueOf(it.getValue(PASS_PRODUCT_CODE_FIELD_NAME)),
              typeRefLabel = it.getMaybeValue(PASS_PRODUCT_LABEL_FIELD_NAME),
              validityStartDate = parseDateOrNull(it.getMaybeValue(PASS_VALIDITY_START_FIELD_NAME)),
              validityEndDate = parseDateOrNull(it.getMaybeValue(PASS_VALIDITY_END_FIELD_NAME))
          )
        }
    }

// we check that we have at least :
// the rail pass number
// the rail pass status
// that the rail pass is REALLY a rail pass ( because you never really know...)
// that the rail pass is active
private fun isActiveAndHasRequiredRailPassFields(allFields: Map<String, String>): Boolean =
  allFields.contains(PASS_NUMBER_FIELD_NAME)
      && ACTIVE_FIELD_VALUE == allFields[PASS_ACTIVE_STATUS_FIELD_NAME]
      && enumContains<PassType>(allFields[PASS_PRODUCT_CODE_FIELD_NAME])

private fun parseDateOrNull(maybeDate: String?): LocalDate? =
  try {
    maybeDate?.let { LocalDate.parse(it) }
  } catch (e: DateTimeParseException) {
    null
  }