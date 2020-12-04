package com.prez.extension

import com.prez.api.dto.CustomerPreferencesProfileResponse
import com.prez.model.CustomerPreferences

fun CustomerPreferences.toCustomerPreferencesProfileResponse():CustomerPreferencesProfileResponse =
        CustomerPreferencesProfileResponse(
                id = id ?: "",
                customerId = customerId,
                seatPreference = seatPreference,
                classPreference = classPreference,
                profileName = profileName,
                language = language
        )