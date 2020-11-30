package com.prez.lib.health.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.util.regex.Pattern
import javax.annotation.PostConstruct

class EnvInfosContributor(private val environment: ConfigurableEnvironment, sensiblePropsNameRegex: String) :
  InfoContributor {

  private val sensiblePropsNamePattern: Pattern = Pattern.compile(sensiblePropsNameRegex)
  private var propertyMap: Map<String, Any>? = null

  companion object {
    private const val OBFUSCATED = "******"
  }

  @PostConstruct
  fun buildPropertyMap() {
    propertyMap = filterSystemProperties() //to keep Map State
  }

  override fun contribute(builder: Info.Builder) {
    builder.withDetail("properties", propertyMap)
  }

  private fun filterSystemProperties(): Map<String, Any> {
    return environment.propertySources
      .filterIsInstance(MapPropertySource::class.java)
      .filter { "systemProperties" != it.name && "systemEnvironment" != it.name }
      .map { MapPropertySource::class.java.cast(it) }
      .flatMap { it.source.keys }
      .distinct()
      .associateBy({ it }) { key: String -> obfuscateSensiblesProps(key) }
    //.collect(Collectors.toMap(Function.identity(), { key: String -> obfuscateSensiblesProps(key) }))
}

private fun obfuscateSensiblesProps(key: String): String {
  return if (sensiblePropsNamePattern.matcher(key.toLowerCase()).matches()) OBFUSCATED else environment.getProperty(key)
}
}


