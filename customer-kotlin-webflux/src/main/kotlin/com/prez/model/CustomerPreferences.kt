package com.prez.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.io.Serializable
import java.util.*

@Document(collection = "preferences")
data class CustomerPreferences(
    @Id val id: String? = null,
    @Indexed val customerId: String,
    val seatPreference: SeatPreference,
    val classPreference: Int,
    val profileName: String,
    val language: Locale? = null
) : Serializable {
    override fun equals(other: Any?) = if (other !is CustomerPreferences) false
        else Objects.equals(customerId, other.customerId)
    override fun hashCode() = Objects.hash(customerId)
}