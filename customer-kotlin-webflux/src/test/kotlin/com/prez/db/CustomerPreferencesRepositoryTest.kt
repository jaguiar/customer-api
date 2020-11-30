package com.prez.db

import com.prez.UsingMongoDB
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.test.context.ActiveProfiles
import java.util.*

@Tag("docker")
@ActiveProfiles(profiles = ["test"])
@DataMongoTest
class CustomerPreferencesRepositoryTest(@Autowired val mongoOperations: ReactiveMongoOperations,
                                        @Autowired val toTest: CustomerPreferencesRepository
) : UsingMongoDB() {

    @AfterEach
    fun after() {
        mongoOperations.dropCollection(CustomerPreferences::class.java).block()
    }


    @Test
    fun   `should create new customer preferences if it does not exist`() {
        // Given an empty collection
        val toCreate = CustomerPreferences(
                customerId = "cid",
                classPreference = 2,
                seatPreference = SeatPreference.NO_PREFERENCE,
                profileName = "corneille",
                language = null
        )

        // When
        val created = toTest.save(toCreate).block()

        // Then
        assertThat(created).isNotNull
        assertThat(created!!.customerId).isNotNull
    }

    @Test
    fun `should update customer preferences if it exists`() {
        // Given
        val existing = CustomerPreferences(
                id = UUID.randomUUID().toString(),
                customerId = "existing",
                classPreference = 2,
                seatPreference = SeatPreference.NO_PREFERENCE,
                profileName = "customProfile",
                language = null
        )
                
        mongoOperations.save(existing).block()
        val toUpdate: CustomerPreferences = existing.copy(seatPreference = SeatPreference.NEAR_WINDOW)

        // When
        val updated = toTest.save(toUpdate).block()

        // Then
        assertThat(updated).isNotNull
        assertThat(updated!!.customerId).isEqualTo("existing")
        assertThat(updated.seatPreference).isEqualTo(SeatPreference.NEAR_WINDOW)
    }

    @Test
    fun `should find customer preferences if it exists`() {
        // Given
        val existingId = UUID.randomUUID().toString()
        val existing = CustomerPreferences(
                id = existingId,
                customerId = "existing",
                classPreference = 2,
                seatPreference = SeatPreference.NO_PREFERENCE,
                profileName = "customProfile",
                language = null
        )
                
        mongoOperations.save(existing)
                .block()

        // When
        val updated = toTest.findById(existingId).block()

        // Then
        assertThat(updated).isNotNull
        assertThat(updated!!.customerId).isEqualTo("existing")
        assertThat(updated.seatPreference).isEqualTo(SeatPreference.NO_PREFERENCE)
        assertThat(updated.profileName).isEqualTo("customProfile")
    }

    @Test
    fun `should not find customer preferences if it does not exist`() {
        // Given an empty collection

        // When
        val notFound = toTest.findById("existing").blockOptional()

        // Then
        assertThat(notFound.isPresent).isFalse
    }

    @Test
    fun `should find all existing preferences for a given customer`() {
        // Given
        val defaultProfile = CustomerPreferences(
                customerId = "existing",
                classPreference = 2,
                seatPreference = SeatPreference.NO_PREFERENCE,
                profileName = "defaultProfile",
                language = null
        )
        val fancyProfile = CustomerPreferences(
                customerId = "existing",
                classPreference = 1,
                seatPreference = SeatPreference.NEAR_WINDOW,
                profileName = "fancyProfile",
                language = null
        )
        mongoOperations.save(defaultProfile).block()
        mongoOperations.save(fancyProfile).block()

        // When
        val preferences = toTest.findByCustomerId("existing").collectList().block()

        // Then

        // Then
        assertThat(preferences).isNotNull
        assertThat(preferences!!.size).isEqualTo(2)
        assertThat(preferences[0].customerId).isEqualTo("existing")
        assertThat(preferences[0].seatPreference).isEqualTo(SeatPreference.NO_PREFERENCE)
        assertThat(preferences[0].profileName).isEqualTo("defaultProfile")
        assertThat(preferences[1].customerId).isEqualTo("existing")
        assertThat(preferences[1].seatPreference).isEqualTo(SeatPreference.NEAR_WINDOW)
        assertThat(preferences[1].profileName).isEqualTo("fancyProfile")
    }
}