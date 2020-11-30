package com.prez.ws.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.prez.model.SeatPreference

data class CreateCustomerPreferencesWSRequest(
    @JsonProperty("seatPreference") val seatPreference: String,
    @JsonProperty("classPreference") val classPreference: Int,
    @JsonProperty("profileName") val profileName: String
)

data class CreateCustomerPreferencesWSResponse(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("seatPreferences")
    val seatPreference: String,
    @JsonProperty("classPreferences")
    val classPreference: Int,
    @JsonProperty("profileName")
    val profileName: String
)
