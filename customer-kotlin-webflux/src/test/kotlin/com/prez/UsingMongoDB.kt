package com.prez

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName

@ContextConfiguration(initializers = [UsingMongoDB.Initializer::class])
open class UsingMongoDB {

  companion object {
    const val MONGODB_PORT = 27017

    @Container
    var mongodb: GenericContainer<Nothing> = GenericContainer<Nothing>(DockerImageName.parse("mongo:4.2"))
        .withExposedPorts(MONGODB_PORT)
  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
      mongodb.withReuse(true) // another difference with Java version
      mongodb.start()
      TestPropertyValues.of(
          "spring.data.mongodb.uri=mongodb://127.0.0.1:" + mongodb.getMappedPort(MONGODB_PORT),
          "spring.data.mongodb.database=customerdbtest"
      ).applyTo(applicationContext.environment)
    }
  }

}
