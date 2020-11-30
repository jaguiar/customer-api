package com.prez;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@ContextConfiguration(initializers = {UsingRedis.Initializer.class})
@Testcontainers
public class UsingRedis {
  public static final int REDIS_PORT = 6379;

  @Container
  public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6.0.9"))
      .withExposedPorts(REDIS_PORT)
      .withReuse(true);

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
      redis.start();

      String redisContainerPort = "spring.redis.port=" + redis.getMappedPort(REDIS_PORT);
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext, redisContainerPort);
    }
  }

}
