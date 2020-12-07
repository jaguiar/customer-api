package com.prez.service

import com.prez.cache.CustomerCacheRepository
import com.prez.db.CustomerPreferencesRepository
import com.prez.exception.NotFoundException
import com.prez.extension.toCustomer
import com.prez.model.Customer
import com.prez.model.CustomerPreferences
import com.prez.model.SeatPreference
import com.prez.ws.CustomerClient
import com.prez.ws.model.CreateCustomerPreferencesWSRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Locale
import java.util.UUID

interface CustomerService {
  suspend fun getCustomerInfo(customerId: String): Customer

  suspend fun createCustomerPreferences(
    customerId: String,
    seatPreference: SeatPreference,
    classPreference: Int,
    profileName: String,
    language: Locale?
  ): CustomerPreferences

  @FlowPreview
  suspend fun getCustomerPreferences(customerId: String): Flow<CustomerPreferences>
}

@Component
class CustomerServiceImpl(
  val customerWebService: CustomerClient,
  val cache: CustomerCacheRepository,
  val database: CustomerPreferencesRepository
) : CustomerService {

  companion object {
    private val logger = LoggerFactory.getLogger(CustomerService::class.java)
  }

  override suspend fun getCustomerInfo(customerId: String): Customer {
    logger.debug("Getting customer with customerId=$customerId")
    return cache.findById(customerId).awaitFirstOrNull()
      ?: callCustomerWebService(customerId)
  }

  private suspend fun callCustomerWebService(customerId: String): Customer {
    val wsResponse = customerWebService.getCustomer(customerId)
      ?: throw NotFoundException(customerId, "customer")
    val customer = wsResponse.toCustomer()
    val savedInCache = cache.save(customer).awaitSingle()

    if (savedInCache) {
      logger.debug("Customer ${customer.customerId} avec in cache")
    } else {
      logger.error("COULD NOT SAVE ${customer.customerId} in cache")
    }
    // then we just return the customer
    return customer
  }

  override suspend fun createCustomerPreferences(customerId: String, seatPreference: SeatPreference,
    classPreference: Int, profileName: String, language: Locale?): CustomerPreferences {
    logger.debug("saveCustomerPreferences : seatPreference \"{}\", classPreference \"{}\" and profileName \"{}\"" +
      " with locale\"{}\" for customer \"{}\"", seatPreference, classPreference, profileName, language, customerId)
    val createCustomerPreferencesRequest = CustomerPreferences(
      UUID.randomUUID().toString(),
      customerId,
      seatPreference,
      classPreference,
      profileName,
      language)
    return database.save(createCustomerPreferencesRequest).awaitSingle()
  }

  @FlowPreview
  override suspend fun getCustomerPreferences(customerId: String): Flow<CustomerPreferences> {
    logger.debug("getCustomerPreferences for customer \"{}\"", customerId)
    return database.findByCustomerId(customerId)
      .asFlow()
  }
}

