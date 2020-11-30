package com.prez.lib.health;

import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.HistogramSupport;
import java.util.Arrays;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class AcceptableMeanIndicator implements HealthIndicator {
  private HistogramSupport metric;

  private double acceptableMean;

  private String name;

  public AcceptableMeanIndicator(HistogramSupport metric, double acceptableMean, String name) {
    this.metric = metric;
    this.acceptableMean = acceptableMean;
    this.name = name;
  }

  @Override
  public Health health() {
    Health.Builder healthStatus = new Health.Builder();
    HistogramSnapshot snapshot = metric.takeSnapshot();
    long size = snapshot.count();
    double mean = snapshot.mean();
    if (size == 0) {
      healthStatus.unknown();
    } else if (mean > acceptableMean) {
      healthStatus.up();
    } else {
      healthStatus.down();
    }
    return healthStatus
        .withDetail("name", name)
        .withDetail("value", mean)
        .withDetail("total", snapshot.total())
        .withDetail("count", size)
        .withDetail("max", snapshot.max())
        .withDetail("percentilesValues", Arrays.toString(snapshot.percentileValues()))
        .build();
  }
}
