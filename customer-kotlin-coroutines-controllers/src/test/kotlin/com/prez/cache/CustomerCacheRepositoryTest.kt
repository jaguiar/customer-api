package com.prez.cache

import com.prez.model.LoyaltyStatus.FFD700
import com.prez.model.PassType.PRO_FIRST
import org.assertj.core.api.Assertions.assertThat

import com.prez.UsingRedis
import com.prez.model.Customer
import com.prez.model.LoyaltyProgram
import com.prez.model.RailPass
import java.time.Duration
import java.time.LocalDate

import com.prez.DockerTest //FIXME do we want to use this annotation?

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.test.context.ActiveProfiles

@Tag("docker")
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@ActiveProfiles(profiles = ["test"])
@SpringBootTest
internal class CustomerCacheRepositoryTest(@Autowired val customerInfoRedisTemplate: ReactiveRedisTemplate<String, Customer>) :
    UsingRedis() {

    private val toTest: CustomerCacheRepository = CustomerCache(4, customerInfoRedisTemplate)

    @BeforeEach
    internal fun beforeEach() {
        customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block()
    }

    @Test
    fun `save should create a customer entry with ttl`() {
        //given
        val customerInfo = Customer(
            customerId = "35adcf57-2cf7-4945-a980-e9753eb146f7",
            email = "mission.impossible@connect.fr",
            firstName = "Jim",
            lastName = "Phelps",
            birthDate = LocalDate.of(1952, 2, 29),
            phoneNumber = null,
            loyaltyProgram = LoyaltyProgram(
                number = "008",
                status = FFD700,
                statusRefLabel = "GOLD IT IS",
                validityStartDate = LocalDate.now(),
                validityEndDate = LocalDate.MAX
            ),
            railPasses = listOf(
                RailPass(
                    number = "JIMID",
                    type = PRO_FIRST,
                    typeRefLabel = "I AM A PRO",
                    validityStartDate = LocalDate.of(2019, 12, 25),
                    validityEndDate = LocalDate.of(2045, 12, 23)
                )
            )
        )

        // Test
        val isSaved = toTest.save(customerInfo).block()
        val ttl = customerInfoRedisTemplate.getExpire("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block()
        val savedCustomer =
            customerInfoRedisTemplate.opsForValue().get("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block()

        // Assert
        assertThat(isSaved).isTrue()
        assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3))
        assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(4))
        assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo)
    }

    @Test
    fun `save should update a customer entry with ttl`() {
        //given
        customerInfoRedisTemplate.opsForValue().set(
            "Customer:35adcf57-2cf7-4945-a980-e9753eb146f7", Customer(
                customerId = "35adcf57-2cf7-4945-a980-e9753eb146f7",
                email = "mission.impossible@connect.fr",
                firstName = "Jim",
                lastName = "Phelps",
                birthDate = LocalDate.of(1952, 2, 29),
                phoneNumber = null,
                loyaltyProgram = LoyaltyProgram(
                    number = "008",
                    status = FFD700,
                    statusRefLabel = "GOLD IT IS",
                    validityStartDate = LocalDate.now(),
                    validityEndDate = LocalDate.MAX
                ),
                railPasses = listOf(
                    RailPass(
                        number = "JIMID",
                        type = PRO_FIRST,
                        typeRefLabel = "I AM A PRO",
                        validityStartDate = LocalDate.of(2019, 12, 25),
                        validityEndDate = LocalDate.of(2045, 12, 23)
                    )
                )
            )
        ).block()
        val customerInfoUpdate = Customer(
            customerId = "35adcf57-2cf7-4945-a980-e9753eb146f7",
            email = "eurostar.helicopter@connect.fr",
            firstName = "Bad",
            lastName = "Phelps",
            birthDate = LocalDate.of(1952, 2, 29),
            phoneNumber = null,
            loyaltyProgram = LoyaltyProgram(
                number = "008",
                status = FFD700,
                statusRefLabel = "GOLD IT IS",
                validityStartDate = LocalDate.now(),
                validityEndDate = LocalDate.MAX
            ),
            railPasses = listOf(
                RailPass(
                    number = "JIMID",
                    type = PRO_FIRST,
                    typeRefLabel = "I AM A PRO",
                    validityStartDate = LocalDate.of(2019, 12, 25),
                    validityEndDate = LocalDate.of(2045, 12, 23)
                )
            )
        )

        // Test
        val isSaved = toTest.save(customerInfoUpdate).block()
        val ttl = customerInfoRedisTemplate.getExpire("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block()
        val savedCustomer =
            customerInfoRedisTemplate.opsForValue().get("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block()

        // Assert
        assertThat(isSaved).isTrue()
        assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3))
        assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(4))
        assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfoUpdate)
    }

    @Test
    fun `findById should find a saved customer by Id`() {
        //given
        val customerInfo = Customer(
            customerId = "35adcf57-2cf7-4945-a980-e9753eb146f7",
            email = "mission.impossible@connect.fr",
            firstName = "Jim",
            lastName = "Phelps",
            birthDate = LocalDate.of(1952, 2, 29),
            phoneNumber = null,
            loyaltyProgram = LoyaltyProgram(
                number = "008",
                status = FFD700,
                statusRefLabel = "GOLD IT IS",
                validityStartDate = LocalDate.now(),
                validityEndDate = LocalDate.MAX
            ),
            railPasses = listOf(
                RailPass(
                    number = "JIMID",
                    type = PRO_FIRST,
                    typeRefLabel = "I AM A PRO",
                    validityStartDate = LocalDate.of(2019, 12, 25),
                    validityEndDate = LocalDate.of(2045, 12, 23)
                )
            )
        )
        customerInfoRedisTemplate.opsForValue().set("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7", customerInfo)
            .block()

        // Test
        val savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").block()

        // Assert
        assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo)
    }

    @Test
    fun `findById should return empty if there is no entry with the provided id`() {
        //given

        // Test
        val savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").block()

        // Assert
        assertThat(savedCustomer).isNull()
    }
}