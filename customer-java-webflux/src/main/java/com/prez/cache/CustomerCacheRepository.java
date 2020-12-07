package com.prez.cache;

import com.prez.model.Customer;
import reactor.core.publisher.Mono;

// because Spring Data does not implement a reactive version of ReactiveCrudRepository for Redis
//https://jira.spring.io/browse/DATAREDIS-831
public interface CustomerCacheRepository /* extends ReactiveCrudRepository<Customer, String> */ {
  /**
   * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
   * entity instance completely.
   *
   * @param entity must not be null.
   * @return [Mono] emitting true if the entity has been set.
   * @throws IllegalArgumentException in case the given `entity` is null.
   */
  Mono<Boolean> save(Customer entity);

  /**
   * Retrieves an entity by its id.
   *
   * @param id must not be null.
   * @return [Mono] emitting the entity with the given id or [Mono.empty] if none found.
   * @throws IllegalArgumentException in case the given `id` is null.
   */
  Mono<Customer> findById(String id);


}

