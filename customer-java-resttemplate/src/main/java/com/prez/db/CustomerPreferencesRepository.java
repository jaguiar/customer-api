package com.prez.db;

import com.prez.model.CustomerPreferences;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerPreferencesRepository extends MongoRepository<CustomerPreferences, String> {
  List<CustomerPreferences> findByCustomerId(String customerId);
}
