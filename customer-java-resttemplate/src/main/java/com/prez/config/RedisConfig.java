package com.prez.config;

import com.prez.model.Customer;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(basePackages = "com.prez.cache", keyspaceConfiguration = RedisConfig.CustomerKeyspaceConfiguration.class)
public class RedisConfig {

  //ok this value *HAS TO STAY HERE* because of the way the KeyspaceConfiguration is instantiated, sorry guys and gals
  @Value("${spring.redis.time-to-live.customer}")
  private long timeToLiveForCustomer;

  @Bean
  public RedisTemplate<String, Customer> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
    RedisTemplate<String, Customer> template = new RedisTemplate<>();
    template.setConnectionFactory(lettuceConnectionFactory);
    return template;
  }

  public class CustomerKeyspaceConfiguration extends KeyspaceConfiguration {

    @Override
    protected Iterable<KeyspaceSettings> initialConfiguration() {
      return Collections.singleton(getCustomerKeyspaceSettings());
    }

    private KeyspaceSettings getCustomerKeyspaceSettings() {
      KeyspaceSettings keyspaceSettings = new KeyspaceSettings(Customer.class, "customer");
      keyspaceSettings.setTimeToLive(timeToLiveForCustomer);
      return keyspaceSettings;
    }
  }
}
