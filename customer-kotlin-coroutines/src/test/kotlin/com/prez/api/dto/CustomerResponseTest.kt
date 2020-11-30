package com.prez.api.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CustomerResponseTest {

    @Test
    fun `CustomerResponse toString should display all fields with null values when no customer info present except customerId`() {
        // Arrange
        val toTest = CustomerResponse(
            customerId = "samsam"
        )

        // Act
        val toString = toTest.toString()

        // Assert
        assertThat(toString).isEqualTo(
            "customerId:samsam, firstName:null, lastName:null, age:null, phoneNumber:null, email:null, loyaltyProgram: null, railPasses:[]"
        )
    }

    @Test
    fun `CustomerResponse toString should display "SET" for personal information when all customer info present`() {
        // Arrange
        val toTest = CustomerResponse(
            customerId = "samsam",
            phoneNumber = "12345",
            firstName = "Samsam",
            lastName = "SuperHeros",
            email = "leplus.grand@des.petits.heros",
            age = 6,
            loyaltyProgram = LoyaltyProgramResponse(
                number = "SamSaucer",
                label = "Samnounours",
                validityStartDate = LocalDate.of(2020, 1, 1),
                validityEndDate = LocalDate.of(2020, 12, 31)
            ),
            railPasses = listOf(
                RailPassResponse(
                    number = "gouzilli",
                    label = "a ions",
                    validityStartDate = LocalDate.of(2019, 1, 1),
                    validityEndDate = LocalDate.of(2021, 1, 1)
                )
            )
        )

        // Act
        val toString = toTest.toString()

        // Assert
        assertThat(toString).isEqualTo(
            "customerId:samsam, firstName:SET, lastName:SET, age:SET, phoneNumber:SET, email:SET, loyaltyProgram: number:SamSaucer, label:Samnounours, validityStartDate:2020-01-01, validityEndDate:2020-12-31, railPasses:[number:gouzilli, label:a ions, validityStartDate:2019-01-01, validityEndDate:2021-01-01]"
        )
    }
}