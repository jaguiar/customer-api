package com.prez.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.prez.model.Customer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.newSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

  @Bean
  fun customerInfoRedisTemplate(
      objectMapper: ObjectMapper,
      factory: LettuceConnectionFactory
  ): ReactiveRedisTemplate<String, Customer> {
    val keySerializer = StringRedisSerializer()
    val valueSerializer = Jackson2JsonRedisSerializer(Customer::class.java)
    val builder = newSerializationContext<String, Customer>(keySerializer)
    valueSerializer.setObjectMapper(objectMapper)
    val context = builder.value(valueSerializer).build()

    return ReactiveRedisTemplate(factory, context)
  }
}