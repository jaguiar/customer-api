package com.prez.api.dto

import com.prez.model.SeatPreference
import java.io.Serializable
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateCustomerPreferencesRequest(
    // For explanation you can refer to this conversation https://github.com/spring-projects/spring-boot/issues/11343
    @field:NotNull(message = "{NotNull.seatPreference}")
    val seatPreference: SeatPreference,
    @field:NotNull(message = "{NotNull.classPreference}")
    @field:Min(value = 1, message = "{Min.classPreference}")
    @field:Max(value = 2, message = "{Max.classPreference}")
    val classPreference: Int,
    @field:NotNull(message = "{NotNull.profileName}")
    @field:Size(min = 1, max = 50, message = "{Size.profileName}")
    @field:Pattern(regexp = "(\\p{IsAlphabetic}|\\s|-)*", message = "{Pattern.profileName}")
    val profileName: String,
    @field:Pattern(message = "{NotValid.language}", regexp = "(:?fr|de|es|en|it|pt)")
    val language: String? = null
) : Serializable