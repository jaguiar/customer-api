package com.prez.lib.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.HistogramSupport;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import java.time.Duration;
import java.util.Map;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class AcceptableMeanIndicatorTest {

  private final static Duration TIMEOUT = Duration.ofSeconds(5);

  @Test
  @DisplayName("health should return UNKNOWN health when there is no value in histogram")
  void health_shouldReturnUnknownHealth_whenNoValueInHistogram() throws Exception {
    //Arrange
    HistogramSupport mockHistogram = mock(HistogramSupport.class);
    when(mockHistogram.takeSnapshot()).thenReturn(
        getMockSnapshot(0l, 0d, 0d, null));
    //Act
    final AcceptableMeanIndicator toTest = new AcceptableMeanIndicator(mockHistogram, 1, "unknownIndicator");
    final Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertThat(result)
        .hasFieldOrPropertyWithValue("status", Status.UNKNOWN)
        .extracting(health -> health.getDetails())
        .isInstanceOfSatisfying(Map.class, map -> assertThat(map).contains(
            MapEntry.entry("name", "unknownIndicator"),
            MapEntry.entry("value", 0.0),
            MapEntry.entry("total", 0.0),
            MapEntry.entry("count", 0L),
            MapEntry.entry("max", 0.0),
            MapEntry.entry("percentilesValues", "[]")));
  }

  @Test
  @DisplayName("health should return UP health when the histogram mean is greater than the acceptable one")
  void health_shouldReturnUpHealth_whenHistogramMeanIsGreaterThanAcceptableMean() throws Exception {
    //Arrange
    HistogramSupport mockHistogram = mock(HistogramSupport.class);
    when(mockHistogram.takeSnapshot()).thenReturn(getMockSnapshot(2l, 4d, 4d, new ValueAtPercentile[] {
        new ValueAtPercentile(50, 2d)}));
    //Act
    final AcceptableMeanIndicator toTest = new AcceptableMeanIndicator(mockHistogram, 1, "goodIndicator");
    final Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertThat(result)
        .hasFieldOrPropertyWithValue("status", Status.UP)
        .extracting(health -> health.getDetails())
        .isInstanceOfSatisfying(Map.class, map -> assertThat(map).contains(
            MapEntry.entry("name", "goodIndicator"),
            MapEntry.entry("value", 2.0),
            MapEntry.entry("total", 4.0),
            MapEntry.entry("count", 2L),
            MapEntry.entry("max", 4.0),
            MapEntry.entry("percentilesValues", "[(2.0 at 5000.0%)]")));

  }

  @Test
  @DisplayName("health should return DOWN health when the histogram mean is lower than the acceptable one")
  void health_shouldReturnDownHealth_whenHistogramMeanIsLesserThanAcceptableMean() throws Exception {
    //Arrange
    HistogramSupport mockHistogram = mock(HistogramSupport.class);
    when(mockHistogram.takeSnapshot()).thenReturn(getMockSnapshot(2l, 0d, 0d, null));
    //Act
    final AcceptableMeanIndicator toTest = new AcceptableMeanIndicator(mockHistogram, 1, "goodIndicator");
    final Health result = toTest.health().block(TIMEOUT);

    //Assert
    assertThat(result)
        .hasFieldOrPropertyWithValue("status", Status.DOWN)
        .extracting(health -> health.getDetails())
        .isInstanceOfSatisfying(Map.class, map -> assertThat(map).contains(
            MapEntry.entry("name", "goodIndicator"),
            MapEntry.entry("value", 0.0),
            MapEntry.entry("total", 0.0),
            MapEntry.entry("count", 2L),
            MapEntry.entry("max", 0.0),
            MapEntry.entry("percentilesValues", "[]")));

  }

  private HistogramSnapshot getMockSnapshot(long count, double total, double max,
                                            ValueAtPercentile[] percentileValues) {
    return new HistogramSnapshot(count, total, max, percentileValues, null, (ps, scaling) -> ps.print("This is a mock"));
  }
}