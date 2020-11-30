package com.prez.db;

import com.prez.model.CustomerPreferences
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface CustomerPreferencesRepository : ReactiveMongoRepository<CustomerPreferences, String> {
    fun findByCustomerId(customerId: String): Flux<CustomerPreferences>
}
