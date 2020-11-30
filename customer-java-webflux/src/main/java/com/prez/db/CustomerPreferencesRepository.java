package com.prez.db;

import com.prez.model.CustomerPreferences;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CustomerPreferencesRepository extends ReactiveMongoRepository<CustomerPreferences, String> {
    Flux<CustomerPreferences> findByCustomerId(String customerId);
}