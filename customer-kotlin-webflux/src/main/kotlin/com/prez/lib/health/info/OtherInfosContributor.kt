package com.prez.lib.health.info

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import java.time.OffsetDateTime

class OtherInfosContributor(private val isSnapshot: Boolean, private val applicationType: String) : InfoContributor {

  companion object {
    private val START_DATE = OffsetDateTime.now()
  }

  override fun contribute(builder: Info.Builder) {
    builder.withDetail("snapshot", isSnapshot)
    builder.withDetail("startDate", START_DATE.toString())
    builder.withDetail("applicationType", applicationType)
  }
}