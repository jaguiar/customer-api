package com.prez.config;

import static org.springframework.data.redis.serializer.RedisSerializationContext.newSerializationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prez.model.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  public ReactiveRedisTemplate<String, Customer> customerInfoRedisTemplate(ObjectMapper objectMapper,
                                                                           LettuceConnectionFactory factory) {
    StringRedisSerializer keySerializer = new StringRedisSerializer();
    Jackson2JsonRedisSerializer<Customer> valueSerializer = new Jackson2JsonRedisSerializer<>(Customer.class);
    RedisSerializationContext.RedisSerializationContextBuilder<String, Customer> builder =
        newSerializationContext(keySerializer);
    valueSerializer.setObjectMapper(objectMapper);
    RedisSerializationContext<String, Customer> context = builder.value(valueSerializer).build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}
