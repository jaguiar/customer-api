package com.prez.extension

import com.prez.api.dto.CustomerResponse
import com.prez.api.dto.LoyaltyProgramResponse
import com.prez.api.dto.RailPassResponse
import com.prez.model.Customer
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
        loyaltyProgram = toLoyaltyProgramResponse(),
        railPasses = toRailPassesResponse()
    )

private fun Customer.toLoyaltyProgramResponse(): LoyaltyProgramResponse? =
    loyaltyProgram?.let {
        LoyaltyProgramResponse(
            number = it.number,
            label = it.statusRefLabel ?: it.status.name,
            validityStartDate = it.validityStartDate,
            validityEndDate = it.validityEndDate
        )
    }

private fun Customer.toRailPassesResponse(): List<RailPassResponse> =
    railPasses.map {
        RailPassResponse(
            number = it.number,
            label = it.typeRefLabel ?: it.type.name,
            validityStartDate = it.validityStartDate,
            validityEndDate = it.validityEndDate
        )
    }
