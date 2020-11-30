package com.prez.cache;

import com.prez.model.Customer;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// because Spring Data does not implement a reactive version of ReactiveCrudRepository for Redis
@Component
public class CustomerCache implements CustomerCacheRepository {


  private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCache.class);
  private static final String CUSTOMER_KEY_SPACE = Customer.class.getSimpleName() + ":";

  private final Duration timeToLive;
  private final ReactiveValueOperations<String, Customer> reactiveValueOps;

  public CustomerCache(@Value("${spring.redis.time-to-live.customer}") Long ttlSeconds,
                       ReactiveRedisOperations<String, Customer> redisTemplate) {
    this.timeToLive = Duration.ofSeconds(ttlSeconds);
    this.reactiveValueOps = redisTemplate.opsForValue();
  }

  @Override
  public Mono<Boolean> save(Customer entity) {
    LOGGER.debug("Saving in cache customer='{}'", entity);
    return reactiveValueOps.set(CUSTOMER_KEY_SPACE + entity.getCustomerId(), entity, timeToLive);
  }

  @Override
  public Mono<Customer> findById(String id) {
    LOGGER.debug("Looking for customer in cache for id='{}'", id);
    return reactiveValueOps.get(CUSTOMER_KEY_SPACE + id);
  }
}
