package com.prez.api.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.prez.model.SeatPreference
import java.util.*

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CustomerPreferencesResponse (
  val profiles: List<CustomerPreferencesProfileResponse>
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class CustomerPreferencesProfileResponse(
  val id: String,
  val customerId: String,
  @JsonFormat(shape = JsonFormat.Shape.STRING) val seatPreference: SeatPreference,
  val classPreference: Int,
  val profileName: String,
  val language: Locale?
)