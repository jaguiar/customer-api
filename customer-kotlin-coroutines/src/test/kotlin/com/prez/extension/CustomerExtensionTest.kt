package com.prez.extension

import com.prez.api.dto.*
import com.prez.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Period.between

class CustomerExtensionTest {

    @Test
    fun `should transform to CustomerResponse`() {
        // Given
        val customerInfo = Customer(
            customerId = "aliveId",
            birthDate = LocalDate.of(1946, 9, 1),
            lastName = "Phelps",
            firstName = "Jim",
            phoneNumber = "06-07-08-09-10",
            email = "mail@mail.com",
            loyaltyProgram = LoyaltyProgram(
                number = "333",
                statusRefLabel = "Orange",
                status = LoyaltyStatus.DBD4E0_B38BB3,
                validityStartDate = LocalDate.of(2020, 12, 31),
                validityEndDate = LocalDate.of(2020, 12, 31)
            ),
            railPasses = listOf(
                RailPass(
                    number = "444",
                    typeRefLabel = "Pro but second",
                    type = PassType.PRO_SECOND,
                    validityStartDate = LocalDate.of(2000, 12, 31),
                    validityEndDate = LocalDate.of(2029, 12, 31)
                )
            )
        )

        // Test
        val actual = customerInfo.toCustomerResponse()

        // When && Then
        assertThat(actual).isNotNull
            .usingRecursiveComparison().isEqualTo(
                CustomerResponse(
                    customerId = "aliveId",
                    age = between(LocalDate.of(1946, 9, 1), LocalDate.now()).years,
                    lastName = "Phelps",
                    firstName = "Jim",
                    email = "mail@mail.com",
                    phoneNumber = "06-07-08-09-10",
                    loyaltyProgram = LoyaltyProgramResponse(
                        number = "333",
                        label = "Orange",
                        validityStartDate = LocalDate.of(2020, 12, 31),
                        validityEndDate = LocalDate.of(2020, 12, 31)
                    ),
                    railPasses = listOf(
                        RailPassResponse(
                            number = "444",
                            label = "Pro but second",
                            validityStartDate = LocalDate.of(2000, 12, 31),
                            validityEndDate = LocalDate.of(2029, 12, 31)
                        )
                    )
                )
            )
    }

    @Test
    fun `should transform to CustomerResponse with loyalty status & rail pass type if labels are missing`() {
        // Given
        val customerInfo = Customer(
            customerId = "aliveId",
            birthDate = LocalDate.of(1946, 9, 1),
            lastName = "Phelps",
            firstName = "Jim",
            phoneNumber = "06-07-08-09-10",
            email = "mail@mail.com",
            loyaltyProgram = LoyaltyProgram(
                number = "333",
                status = LoyaltyStatus.DBD4E0_B38BB3,
                validityStartDate = LocalDate.of(2020, 12, 31),
                validityEndDate = LocalDate.of(2020, 12, 31)
            ),
            railPasses = listOf(
                RailPass(
                    number = "444",
                    type = PassType.PRO_SECOND,
                    validityStartDate = LocalDate.of(2000, 12, 31),
                    validityEndDate = LocalDate.of(2029, 12, 31)
                )
            )
        )

        // Test
        val actual = customerInfo.toCustomerResponse()

        // When && Then
        assertThat(actual).isNotNull
            .usingRecursiveComparison().isEqualTo(
                CustomerResponse(
                    customerId = "aliveId",
                    age = between(LocalDate.of(1946, 9, 1), LocalDate.now()).years,
                    lastName = "Phelps",
                    firstName = "Jim",
                    email = "mail@mail.com",
                    phoneNumber = "06-07-08-09-10",
                    loyaltyProgram = LoyaltyProgramResponse(
                        number = "333",
                        label = "DBD4E0_B38BB3",
                        validityStartDate = LocalDate.of(2020, 12, 31),
                        validityEndDate = LocalDate.of(2020, 12, 31)
                    ),
                    railPasses = listOf(
                        RailPassResponse(
                            number = "444",
                            label = "PRO_SECOND",
                            validityStartDate = LocalDate.of(2000, 12, 31),
                            validityEndDate = LocalDate.of(2029, 12, 31)
                        )
                    )
                )
            )
    }

    @Test
    fun `should transform to CustomerResponse with no age when birthDate is missing`() {
        // Given
        val customerInfo = Customer(
            customerId = "aliveId",
            lastName = "Phelps",
            firstName = "Jim",
            birthDate = null,
            phoneNumber = "06-07-08-09-10",
            email = "mail@mail.com"
        )

        // Test
        val actual = customerInfo.toCustomerResponse()

        // When && Then
        assertThat(actual).isNotNull
            .usingRecursiveComparison().isEqualTo(
                CustomerResponse(
                    customerId = "aliveId",
                    lastName = "Phelps",
                    firstName = "Jim",
                    age = null,
                    email = "mail@mail.com",
                    phoneNumber = "06-07-08-09-10"
                )
            )
    }
}
