package com.prez.ws.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.StringUtils.isNotBlank

data class GetCustomerWSResponse(
        val id: String,
        val personalInformation: PersonalInformation?,
        val personalDetails: PersonalDetails?,
        val cards: Cards = Cards(listOf()),
        val services: Services? = null,
        val photos: Photos? = null,
        val misc: List<Misc> = listOf()
)

data class NestedValue(
        val value: String
)

data class PersonalInformation(
        val civility: NestedValue?,
        val lastName: String?,
        val firstName: String?,
        val birthdate: String?,
        val alive: Boolean?
)

data class PersonalDetails(
        val email: Email?,
        val cell: Cell?
)

data class Email(
        val address: String?,
        val default: Boolean = false,
        val confirmed: NestedValue? = null
)

data class Cell(
        val number: String?
)

data class Cards(
        @JsonProperty("records")
        val cards: List<Card> = listOf()
)

data class Card(
        val number: String?,
        val type: NestedValue?,
        val ticketless: Boolean,
        val disableStatus: NestedValue?
)

data class Services(
        val list: List<Service> = listOf()
)

data class Service(
        val name: NestedValue?,
        val status: NestedValue?,
        val updatedTime: String?
)

data class Photos(
        val file: File?
)

data class File(
        @JsonProperty("@id")
        val id: String
)

data class Misc(
        val type: NestedValue?,
        val count: Int,
        val hasMore: Boolean,
        val records: List<Record> = listOf()
)

data class Record(
        @JsonProperty("otherId")
        val otherId: String?,
        val type: NestedValue?,
        @JsonProperty("fields") // ici en java on avait mis un ListOfObjectsToMapDeserializer
        private val map: List<Map<String, String>> = listOf()
) {
    val mapAsRealMap: Map<String, String> by lazy { //by lazy is not mandatory, could be useful for perfs
        //something more usable for mapping misc properties
        map
                .filter { isNotBlank(it["key"]) && isNotBlank(it["value"]) }
                .map { it.getValue("key") to it.getValue("value") }
                .toMap()
    }

    fun getValue(key: String): String = mapAsRealMap.getValue(key)
    fun getMaybeValue(key: String): String? = mapAsRealMap[key]

}