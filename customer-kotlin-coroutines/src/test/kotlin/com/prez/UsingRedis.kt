package com.prez

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@ContextConfiguration(initializers = [UsingRedis.Initializer::class])
open class UsingRedis {

  companion object {
    const val REDIS_PORT = 6379

    @Container
    var redis: GenericContainer<Nothing> = GenericContainer<Nothing>(DockerImageName.parse("redis:6.2"))
      .withExposedPorts(REDIS_PORT)
  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
      redis.withReuse(true) // another difference with Java version
      redis.start()
      TestPropertyValues.of(
        "spring.redis.port=" + redis.getMappedPort(REDIS_PORT)
      ).applyTo(applicationContext.environment)
    }
  }

}
