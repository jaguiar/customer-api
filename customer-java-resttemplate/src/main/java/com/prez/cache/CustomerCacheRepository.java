package com.prez.cache;

import com.prez.model.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerCacheRepository extends CrudRepository<Customer, String> {


}

