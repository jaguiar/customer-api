package com.prez.lib.health

import io.micrometer.core.instrument.distribution.HistogramSnapshot
import io.micrometer.core.instrument.distribution.HistogramSupport
import io.micrometer.core.instrument.distribution.ValueAtPercentile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.actuate.health.Status
import java.io.PrintStream
import java.time.Duration

class AcceptableMeanIndicatorTest {

  companion object {
    private val TIMEOUT = Duration.ofSeconds(5)
  }

  @Test
  fun `health should return UNKNOWN health when there is no value in histogram`() {
    //Arrange
    val mockHistogram = mock(HistogramSupport::class.java)
    `when`(mockHistogram.takeSnapshot()).thenReturn(
      getMockSnapshot(0L, 0.0, 0.0, null)
    )
    //Act
    val toTest = AcceptableMeanIndicator(mockHistogram, 1.0, "unknownIndicator")
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertThat(result)
      .hasFieldOrPropertyWithValue("status", Status.UNKNOWN)
    val details = result.details
    assertThat(details)
      .contains(
        MapEntry.entry("name", "unknownIndicator"),
        MapEntry.entry("value", 0.0),
        MapEntry.entry("total", 0.0),
        MapEntry.entry("count", 0L),
        MapEntry.entry("max", 0.0),
        MapEntry.entry("percentilesValues", "[]")
      )
  }

  @Test
  fun `health should return UP health when the histogram mean is greater than the acceptable one`() {
    //Arrange
    val mockHistogram = mock(HistogramSupport::class.java)
    `when`(mockHistogram.takeSnapshot()).thenReturn(
      getMockSnapshot(
        2L, 4.0, 4.0, arrayOf(
          ValueAtPercentile(50.0, 2.0)
        )
      )
    )
    //Act
    val toTest = AcceptableMeanIndicator(mockHistogram, 1.0, "goodIndicator")
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertThat(result)
      .hasFieldOrPropertyWithValue("status", Status.UP)
    val details = result.details
    assertThat(details)
      .contains(
        MapEntry.entry("name", "goodIndicator"),
        MapEntry.entry("value", 2.0),
        MapEntry.entry("total", 4.0),
        MapEntry.entry("count", 2L),
        MapEntry.entry("max", 4.0),
        MapEntry.entry("percentilesValues", "[(2.0 at 5000.0%)]")
      )
  }

  @Test
  fun `health should return DOWN health when the histogram mean is lower than the acceptable one`() {
    //Arrange
    val mockHistogram = mock(HistogramSupport::class.java)
    `when`(mockHistogram.takeSnapshot()).thenReturn(getMockSnapshot(2L, 0.0, 0.0, null))
    //Act
    val toTest = AcceptableMeanIndicator(mockHistogram, 1.0, "goodIndicator")
    val result = toTest.health().block(TIMEOUT)

    //Assert
    assertThat(result)
      .hasFieldOrPropertyWithValue("status", Status.DOWN)
    val details = result.details
    assertThat(details).contains(
      MapEntry.entry("name", "goodIndicator"),
      MapEntry.entry("value", 0.0),
      MapEntry.entry("total", 0.0),
      MapEntry.entry("count", 2L),
      MapEntry.entry("max", 0.0),
      MapEntry.entry("percentilesValues", "[]")
    )
  }

  private fun getMockSnapshot(
    count: Long, total: Double, max: Double,
    percentileValues: Array<ValueAtPercentile>?
  ): HistogramSnapshot {
    return HistogramSnapshot(
      count,
      total,
      max,
      percentileValues,
      null
    ) { ps, _ -> ps.print("This is a mock") }
  }
}