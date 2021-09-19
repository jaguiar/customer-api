package com.prez;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@ContextConfiguration(initializers = {UsingMongoDBAndRedis.Initializer.class})
public class UsingMongoDBAndRedis {
    public static final int MONGODB_PORT = 27017;
    public static final int REDIS_PORT = 6379;

    @Container
    public static GenericContainer<?> mongodb = new GenericContainer<>(DockerImageName.parse("mongo:4.2"))
            .withExposedPorts(MONGODB_PORT); // we don't reuse container for integration tests because we get timeouts

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6.2"))
            .withExposedPorts(REDIS_PORT); // we don't reuse container for integration tests because we get timeouts

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            redis.start();
            mongodb.start();

            TestPropertyValues.of(
                    "spring.data.mongodb.uri=mongodb://127.0.0.1:" + mongodb.getMappedPort(MONGODB_PORT),
                    "spring.data.mongodb.database=customerdbtest",
                    "spring.redis.port=" + redis.getMappedPort(REDIS_PORT)
            ).applyTo(applicationContext.getEnvironment());
        }
    }

}
