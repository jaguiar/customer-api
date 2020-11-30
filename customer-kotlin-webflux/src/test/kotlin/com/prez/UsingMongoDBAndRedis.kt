package com.prez

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@ContextConfiguration(initializers = [UsingMongoDBAndRedis.Initializer::class])
open class UsingMongoDBAndRedis {

  companion object {
    const val MONGODB_PORT = 27017
    const val REDIS_PORT = 6379

    @Container
    var mongodb: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse("mongo:4.0"))
        .withExposedPorts(MONGODB_PORT)

    @Container
    var redis: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse("redis:6.0.9"))
        .withExposedPorts(UsingRedis.REDIS_PORT)
  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
      // redis.withReuse(true) // we don't reuse container for integration tests because we get timeouts
      redis.start()
      // mongodb.withReuse(true)
      mongodb.start()
      TestPropertyValues.of(
          "spring.data.mongodb.uri=mongodb://127.0.0.1:" + mongodb.getMappedPort(MONGODB_PORT),
          "spring.data.mongodb.database=customerdbtest",
          "spring.redis.port=" + redis.getMappedPort(REDIS_PORT)
      ).applyTo(applicationContext.environment)
    }
  }

}
