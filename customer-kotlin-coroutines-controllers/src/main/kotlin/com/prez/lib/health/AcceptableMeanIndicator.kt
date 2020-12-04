package com.prez.lib.health

import io.micrometer.core.instrument.distribution.HistogramSupport
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import reactor.core.publisher.Mono
import java.util.Arrays

class AcceptableMeanIndicator(
  private val metric: HistogramSupport,
  private val acceptableMean: Double,
  private val name: String
) : ReactiveHealthIndicator {

  override fun health(): Mono<Health> {
    val healthStatus = Health.Builder()
    val snapshot = metric.takeSnapshot()
    val size = snapshot.count()
    val mean = snapshot.mean()
    when {
      (size == 0L) -> healthStatus.unknown()
      (mean > acceptableMean) -> healthStatus.up()
      else -> healthStatus.down()
    }
    return Mono.just(
      healthStatus
        .withDetail("name", name)
        .withDetail("value", mean)
        .withDetail("total", snapshot.total())
        .withDetail("count", size)
        .withDetail("max", snapshot.max())
        .withDetail("percentilesValues", Arrays.toString(snapshot.percentileValues()))
        .build()
    )
  }
}