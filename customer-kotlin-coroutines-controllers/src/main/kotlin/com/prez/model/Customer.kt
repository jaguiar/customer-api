package com.prez.model

import java.time.LocalDate

data class Customer(
    val customerId: String,
    val lastName: String?,
    val firstName: String?,
    val birthDate: LocalDate?,
    val phoneNumber: String?,
    val email: String?,
    val loyaltyProgram: LoyaltyProgram? = null,
    val railPasses: List<RailPass> = emptyList()
)

data class LoyaltyProgram(
    val number: String,
    val status: LoyaltyStatus,
    val statusRefLabel: String? = null,
    val validityStartDate: LocalDate?,
    val validityEndDate: LocalDate?
) 

enum class LoyaltyStatus {
    CD7F32,
    E0E0E0,
    FFD700,
    B0B0B0,
    _019875,
    DBD4E0_B38BB3
}

data class RailPass(
    val number: String,
    val type: PassType,
    val typeRefLabel: String? = null,
    val validityStartDate: LocalDate?,
    val validityEndDate: LocalDate?
){
   /* val temporalStatus: TemporalStatus by lazy{
        val dateNow = LocalDate.now()
        if(validityEndDate < dateNow)
            TemporalStatus.EXPIRED
        else if(validityStartDate > dateNow)
            TemporalStatus.FUTUR
        else
            TemporalStatus.ONGOING
    }*/
}


enum class PassType {
    YOUTH,
    FAMILY,
    SENIOR,
    PRO_SECOND,
    PRO_FIRST,
    FROM_OUTER_SPACE;
}
/*
enum class TemporalStatus(val status: Int){
    ONGOING(1),
    FUTUR(2),
    EXPIRED(3);

}
*/