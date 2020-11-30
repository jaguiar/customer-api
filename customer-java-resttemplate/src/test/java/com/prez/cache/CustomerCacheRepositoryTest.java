package com.prez.cache;

import static com.prez.model.LoyaltyStatus.FFD700;
import static com.prez.model.PassType.PRO_FIRST;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.prez.UsingRedis;
import com.prez.config.RedisConfig;
import com.prez.model.Customer;
import com.prez.model.LoyaltyProgram;
import com.prez.model.RailPass;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.keyvalue.core.KeyValueAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;

@Tag("docker")
@DataRedisTest
@ContextConfiguration(initializers = CustomerCacheRepositoryTest.Initializer.class, classes = RedisConfig.class)
class CustomerCacheRepositoryTest extends UsingRedis {

  @Autowired
  private KeyValueAdapter redisKeyspace;

  @Autowired
  private CustomerCacheRepository toTest;


  @AfterEach
  void afterEach() {
    redisKeyspace.deleteAllOf("customer");
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
    final Customer saved = toTest.save(customerInfo);
    final Customer savedCustomer =
        (Customer) redisKeyspace.get("35adcf57-2cf7-4945-a980-e9753eb146f7", "customer");

    // Assert
    assertThat(saved).isNotNull();
    /* FIXME TTL
    final Long ttl = redisTemplate.getExpire("Customer:35adcf57-2cf7-4945-a980-e9753eb146f7");
    assertThat(ttl).isGreaterThanOrEqualTo(3L);
    assertThat(ttl).isLessThanOrEqualTo(4L);*/
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo);

  }

  @Test
  @DisplayName("save should update a customer entry with ttl")
  void save_should_update_a_customer_entry_with_ttl() {
    //given
    redisKeyspace.put("35adcf57-2cf7-4945-a980-e9753eb146f7", Customer.builder()
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
        )).build(),
        "customer"
    );
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
    final Customer saved = toTest.save(customerInfoUpdate);
    final Customer expectedCustomer =
        (Customer) redisKeyspace.get("35adcf57-2cf7-4945-a980-e9753eb146f7", "customer");

    // Assert
    assertThat(saved).isNotNull();
    /* FIXME TTL tsts
    final Long ttl = saved.;
    assertThat(ttl).isGreaterThanOrEqualTo(3L);
    assertThat(ttl).isLessThanOrEqualTo(4L);*/
    assertThat(expectedCustomer).usingRecursiveComparison().isEqualTo(customerInfoUpdate);
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
    redisKeyspace.put("35adcf57-2cf7-4945-a980-e9753eb146f7", customerInfo, "customer");

    // Test
    final Customer savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").orElse(null);

    // Assert
    assertThat(savedCustomer).usingRecursiveComparison().isEqualTo(customerInfo);
  }

  @Test
  @DisplayName("findById should return empty if there is no entry with the provided id")
  void findById_should_return_empty_when_no_entry_found_corresponding_to_provided_id() {
    //given

    // Test
    Customer savedCustomer = toTest.findById("35adcf57-2cf7-4945-a980-e9753eb146f7").orElse(null);

    // Assert
    assertThat(savedCustomer).isNull();
  }

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, 
          "spring.redis.time-to-live.customer=4");
    }
  }
}
