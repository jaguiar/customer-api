package com.prez;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@ContextConfiguration(initializers = {UsingMongoDB.Initializer.class})
public class UsingMongoDB {
    public static final int MONGODB_PORT = 27017;

    @Container
    public static GenericContainer<?> mongodb = new GenericContainer<>(DockerImageName.parse("mongo:4.0"))
            .withExposedPorts(MONGODB_PORT)
            .withReuse(true);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            mongodb.start();

            TestPropertyValues.of(
                    "spring.data.mongodb.uri=mongodb://127.0.0.1:" + mongodb.getMappedPort(MONGODB_PORT),
                    "spring.data.mongodb.database=customerdbtest"
            ).applyTo(applicationContext.getEnvironment());
        }
    }

}
