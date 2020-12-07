package com.prez.lib.health.info;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.info.Info;

class OtherInfosContributorTest {


  @Test
  @DisplayName("contribute should contribute snapshot=True, startDate And ApplicationType when called")
  void contribute_shouldContributeSnapshotTrueStartDateAndResourceName_whenCalled() {
    //Arrange
    final Info.Builder infoBuilder = new Info.Builder();
    final OtherInfosContributor toTest = new OtherInfosContributor(true, "A great version for sure");

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails())
        .isNotNull()
        .containsEntry("snapshot", true)
        .containsKey("startDate")
        .containsEntry("applicationType", "A great version for sure");
  }

  @Test
  @DisplayName("contribute should contribute snapshot=False, startDate And ApplicationType when called")
  void contribute_shouldContributeSnapshotFalseStartDateAndResourceName_whenCalled()  {
    //Arrange
    final Info.Builder infoBuilder = new Info.Builder();
    final OtherInfosContributor toTest = new OtherInfosContributor(false, "Just a version");

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails())
        .isNotNull()
        .containsEntry("snapshot", false)
        .containsKey("startDate")
        .containsEntry("applicationType", "Just a version");
  }
}