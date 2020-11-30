package com.prez.lib.health;

import com.prez.lib.health.info.EnvInfosContributor;
import com.prez.lib.health.info.OtherInfosContributor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class ContributorsConfig {

    @Bean(name = "otherInfosContributor")
    public InfoContributor otherInfosContributor(@Value("#{'${info.version-qualifier}' matches 'SNAPSHOT'}") boolean isSnapshot,
        @Value("${info.application-type}") String applicationType) {
        return new OtherInfosContributor(isSnapshot, applicationType);
    }

    @Bean(name = "envInfosContributor")
    public InfoContributor envInfosContributor(ConfigurableEnvironment environment,
                                               @Value("${management.endpoints.info.sensiblePropsNameRegex:.*(?:password|passwd|pwd|secret).*}") String sensiblePropsNameRegex) {
        return new EnvInfosContributor(environment, sensiblePropsNameRegex);
    }
}

