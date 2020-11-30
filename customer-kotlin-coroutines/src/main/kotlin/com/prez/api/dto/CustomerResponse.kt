package com.prez.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CustomerResponse(
    val customerId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val age: Int? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val loyaltyProgram: LoyaltyProgramResponse? = null,
    val railPasses: List<RailPassResponse> = emptyList(),
) {
    override fun toString(): String {
        return "customerId:${customerId}" +
            ", firstName:${firstName?.let { "SET" }}" +
            ", lastName:${lastName?.let { "SET" }}" +
            ", age:${age?.let { "SET" }}" +
            ", phoneNumber:${phoneNumber?.let { "SET" }}" +
            ", email:${email?.let { "SET" }}" +
            ", loyaltyProgram: ${loyaltyProgram}" +
            ", railPasses:${railPasses}"
    }
}

data class LoyaltyProgramResponse(
    val number: String,
    val label: String,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    val validityStartDate: LocalDate? = null,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    val validityEndDate: LocalDate? = null
) {
    override fun toString(): String {
        return "number:${number}, label:${label}, validityStartDate:${validityStartDate}, validityEndDate:${validityEndDate}"
    }
}

data class RailPassResponse(
    val number: String,
    val label: String,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    val validityStartDate: LocalDate? = null,
    @get:JsonFormat(shape = JsonFormat.Shape.STRING)
    val validityEndDate: LocalDate? = null
) {
    override fun toString(): String {
        return "number:${number}, label:${label}, validityStartDate:${validityStartDate}, validityEndDate:${validityEndDate}"
    }
}
