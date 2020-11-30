package com.prez.lib.health.info

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.info.Info

class OtherInfosContributorTest {
  @Test
  fun `contribute should contribute snapshot=True, startDate And ApplicationType when called`() {
    //Arrange
    val infoBuilder = Info.Builder()
    val toTest = OtherInfosContributor(true, "A great version for sure")

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    assertThat(result.details)
      .isNotNull
      .containsEntry("snapshot", true)
      .containsKey("startDate")
      .containsEntry("applicationType", "A great version for sure")
  }

  @Test
  fun `contribute should contribute snapshot=False, startDate And ApplicationType when called`() {
    //Arrange
    val infoBuilder = Info.Builder()
    val toTest = OtherInfosContributor(false, "Just a version")

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    assertThat(result.details)
      .isNotNull
      .containsEntry("snapshot", false)
      .containsKey("startDate")
      .containsEntry("applicationType", "Just a version")
  }
}