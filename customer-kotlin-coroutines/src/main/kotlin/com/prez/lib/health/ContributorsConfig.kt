package com.prez.lib.health

import com.prez.lib.health.info.EnvInfosContributor
import com.prez.lib.health.info.OtherInfosContributor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment

@Configuration
class ContributorsConfig {

  @Bean(name = ["otherInfosContributor"])
  fun otherInfosContributor(
    @Value("#{'\${info.version-qualifier}' matches 'SNAPSHOT'}") isSnapshot: Boolean,
    @Value("\${info.application-type}") applicationType: String
  ): InfoContributor {
    return OtherInfosContributor(isSnapshot, applicationType)
  }

  @Bean(name = ["envInfosContributor"])
  fun envInfosContributor(
    environment: ConfigurableEnvironment,
    @Value("\${management.endpoints.info.sensiblePropsNameRegex:.*(?:password|passwd|pwd|secret).*}") sensiblePropsNameRegex: String
  ): InfoContributor {
    return EnvInfosContributor(environment, sensiblePropsNameRegex)
  }
}