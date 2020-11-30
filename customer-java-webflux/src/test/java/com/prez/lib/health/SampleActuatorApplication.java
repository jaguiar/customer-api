package com.prez.lib.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
public class SampleActuatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleActuatorApplication.class, args);
    }

}