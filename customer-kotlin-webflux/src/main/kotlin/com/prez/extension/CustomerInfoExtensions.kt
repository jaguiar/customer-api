package com.prez.extension

import com.prez.api.dto.CustomerResponse
import com.prez.api.dto.LoyaltyProgramResponse
import com.prez.api.dto.RailPassResponse
import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.RailPass
import java.time.LocalDate
import java.time.Period.between

/**
 * Extension functions ( mapper mostly ) for model objects
 */

fun Customer.toCustomerResponse() =
  CustomerResponse(
    customerId = customerId,
    lastName = lastName,
    firstName = firstName,
    phoneNumber = phoneNumber,
    email = email,
    age = birthDate?.let { between(it, LocalDate.now()).years },
    loyaltyProgram = loyaltyProgram?.let { it.toLoyaltyProgramResponse() },
    railPasses = railPasses.map { it.toRailPassResponse() }
  )

fun LoyaltyProgram.toLoyaltyProgramResponse(): LoyaltyProgramResponse? =
  LoyaltyProgramResponse(
    number = number,
    label = statusRefLabel ?: status.name,
    validityStartDate = validityStartDate,
    validityEndDate = validityEndDate
  )


fun RailPass.toRailPassResponse(): RailPassResponse =
  RailPassResponse(
    number = number,
    label = typeRefLabel ?: type.name,
    validityStartDate = validityStartDate,
    validityEndDate = validityEndDate
  )
