package com.prez.cache;

import static com.prez.model.LoyaltyStatus.FFD700;
import static com.prez.model.PassType.PRO_FIRST;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.prez.UsingRedis;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.RailPass;
import java.time.Duration;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Tag("docker")
@SpringBootTest
@ActiveProfiles("test")
class CustomerCacheRepositoryTest extends UsingRedis {

  @Autowired
  private ReactiveRedisTemplate<String, Customer> customerInfoRedisTemplate;

  private CustomerCacheRepository toTest;


  @BeforeEach
  void beforeEach() {
    toTest = new CustomerCache(4L, customerInfoRedisTemplate);
    customerInfoRedisTemplate.delete(customerInfoRedisTemplate.keys("Customer:*")).block();
  }

  @Test
  @DisplayName("save should create a customer entry with ttl")
  void save_should_create_a_customer_entry_with_ttl() {
    //given
    final Customer customerInfo = Customer.builder()
        .customerId("35adcf57-2cf7-4945-a980-e9753eb146f7")
        .email("mission.impossible@connect.fr")
        .firstName("Jim")
        .lastName("Phelps")
        .birthDate(LocalDate.of(1952, 2, 29))
        .phoneNumber(null)
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(FFD700)
            .statusRefLabel("GOLD IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .railPasses(singletonList(
            RailPass.builder()
                .number("JIMID")
                .type(PRO_FIRST)
                .typeRefLabel("I AM A PRO")
                .validityStartDate(LocalDate.of(2019, 12, 25))
                .validityEndDate(LocalDate.of(2045, 12, 23))
                .build()
        ))
        .build();

    // Test
    final Boolean isSaved = toTest.save(customerInfo).block();
    final Duration ttl = customerInfoRedisTemplate.getExpire("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block();
    final Customer savedCustomer =
        customerInfoRedisTemplate.opsForValue().get("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block();

    // Assert
    assertThat(isSaved).isTrue();
    assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3));
    assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(4));
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo);

  }

  @Test
  @DisplayName("save should update a customer entry with ttl")
  void save_should_update_a_customer_entry_with_ttl() {
    //given
    customerInfoRedisTemplate.opsForValue().set("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7", Customer.builder()
        .customerId("35adcf57-2cf7-4945-a980-e9753eb146f7")
        .email("mission.impossible@connect.fr")
        .firstName("Jim")
        .lastName("Phelps")
        .birthDate(LocalDate.of(1952, 2, 29))
        .phoneNumber(null)
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(FFD700)
            .statusRefLabel("GOLD IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .railPasses(singletonList(
            RailPass.builder()
                .number("JIMID")
                .type(PRO_FIRST)
                .typeRefLabel("I AM A PRO")
                .validityStartDate(LocalDate.of(2019, 12, 25))
                .validityEndDate(LocalDate.of(2045, 12, 23))
                .build()
        )).build()
    ).block();
    final Customer customerInfoUpdate = Customer.builder()
        .customerId("35adcf57-2cf7-4945-a980-e9753eb146f7")
        .email("eurostar.helicopter@connect.fr")
        .firstName("Bad")
        .lastName("Phelps")
        .birthDate(LocalDate.of(1952, 2, 29))
        .phoneNumber(null)
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(FFD700)
            .statusRefLabel("GOLD IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .railPasses(singletonList(
            RailPass.builder()
                .number("JIMID")
                .type(PRO_FIRST)
                .typeRefLabel("I AM A PRO")
                .validityStartDate(LocalDate.of(2019, 12, 25))
                .validityEndDate(LocalDate.of(2045, 12, 23))
                .build()
        ))
        .build();


    // Test
    final Boolean isSaved = toTest.save(customerInfoUpdate).block();
    final Duration ttl = customerInfoRedisTemplate.getExpire("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block();
    final Customer savedCustomer =
        customerInfoRedisTemplate.opsForValue().get("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7").block();

    // Assert
    assertThat(isSaved).isTrue();
    assertThat(ttl).isGreaterThanOrEqualTo(Duration.ofSeconds(3));
    assertThat(ttl).isLessThanOrEqualTo(Duration.ofSeconds(4));
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfoUpdate);
  }

  @Test
  @DisplayName("findById should find a saved customer by Id")
  void findById_should_find_a_saved_customer_by_id() {
    //given
    final Customer customerInfo = Customer.builder()
        .customerId("35adcf57-2cf7-4945-a980-e9753eb146f7")
        .email("mission.impossible@connect.fr")
        .firstName("Jim")
        .lastName("Phelps")
        .birthDate(LocalDate.of(1952, 2, 29))
        .phoneNumber(null)
        .loyaltyProgram(LoyaltyProgram.builder()
            .number("008")
            .status(FFD700)
            .statusRefLabel("GOLD IT IS")
            .validityStartDate(LocalDate.now())
            .validityEndDate(LocalDate.MAX)
            .build())
        .railPasses(singletonList(
            RailPass.builder()
                .number("JIMID")
                .type(PRO_FIRST)
                .typeRefLabel("I AM A PRO")
                .validityStartDate(LocalDate.of(2019, 12, 25))
                .validityEndDate(LocalDate.of(2045, 12, 23))
                .build()
        ))
        .build();
    customerInfoRedisTemplate.opsForValue().set("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7", customerInfo).block();

    // Test
    final Customer savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").block();

    // Assert
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo);
  }

  @Test
  @DisplayName("findById should return empty if there is no entry with the provided id")
  void findById_should_return_empty_when_no_entry_found_corresponding_to_provided_id() {
    //given

    // Test
    Customer savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").block();

    // Assert
    assertThat(savedCustomer).isNull();
  }
}
