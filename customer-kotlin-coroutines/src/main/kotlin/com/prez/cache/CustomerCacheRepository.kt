package com.prez.cache

import com.prez.model.Customer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

// because Spring Data does not implement a reactive version of ReactiveCrudRepository for Redis
interface CustomerCacheRepository { /* FIXME Is the above still true today ? extends ReactiveCrudRepository<Customer, String> */
    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be null.
     * @return [Mono] emitting true if the entity has been set.
     * @throws IllegalArgumentException in case the given `entity` is null.
     */
    fun save(entity: Customer): Mono<Boolean>

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be null.
     * @return [Mono] emitting the entity with the given id or [Mono.empty] if none found.
     * @throws IllegalArgumentException in case the given `id` is null.
     */
    fun findById(id: String): Mono<Customer>

}

@Component
class CustomerCache(
    @Value("\${spring.redis.time-to-live.customer}") ttlSeconds: Long,
    redisTemplate: ReactiveRedisTemplate<String, Customer>
) : CustomerCacheRepository {

    companion object {
        private val logger = LoggerFactory.getLogger(CustomerCache::class.java)
        private val customerKeyspaceName = "${Customer::class.java.simpleName}:"
    }

    private val reactiveValueOps = redisTemplate.opsForValue();
    private val timeToLive = Duration.ofSeconds(ttlSeconds);

    override fun save(entity: Customer): Mono<Boolean> {
        logger.debug("Saving in cache customer='$entity'")
        return reactiveValueOps.set("$customerKeyspaceName${entity.customerId}", entity, timeToLive)
    }

    override fun findById(id: String): Mono<Customer> {
        logger.debug("Looking for customer in cache for id='$id'")
        return reactiveValueOps.get("$customerKeyspaceName${id}");
    }
}
