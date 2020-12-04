package com.prez.lib.health.info

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.MapEntry.entry
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.info.Info
import org.springframework.mock.env.MockEnvironment

class EnvInfosContributorTest {

  @Test
  fun `contribute should contribute some env Infos when called`() {
    //Arrange
    val mockEnv = MockEnvironment()
      .withProperty("Foo", "Bar")
      .withProperty("PIF", "PAF")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, "")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    val details = result.details["properties"] as Map<String, String>
    assertThat(details)
      .isNotNull
      .hasSize(2)
      .contains(
        entry("Foo", "Bar"),
        entry("PIF", "PAF")
      )
  }

  @Test
  fun `contribute should filter system properties and env when called`() {
    //Arrange
    val mockEnv = MockEnvironment()
    System.setProperty("Tchoup", "Tchip")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, "")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    assertThat(result.details["properties"])
      .isNotNull
      .isInstanceOfSatisfying(
        Map::class.java
      ) { assertThat(it).isEmpty() }
  }

  @Test
  fun `contribute should resolve properties when called`() {
    //Arrange
    val mockEnv = MockEnvironment()
      .withProperty("FirstRule", "You do not talk about \${do-not-talk-about}")
      .withProperty("do-not-talk-about", "Fight Club")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, "")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    val details = result.details["properties"] as Map<String, String>
    assertThat(details)
      .isNotNull
      .hasSize(2)
      .contains(
        entry("do-not-talk-about", "Fight Club"),
        entry("FirstRule", "You do not talk about Fight Club")
      )
  }

  @Test
  fun `contribute should handle duplicated values when called`() {
    //Arrange
    val mockEnv = MockEnvironment()
      .withProperty("FirstRule", "\${FirstRule}")
      .withProperty("FirstRule", "You do not talk about \${do-not-talk-about}")
      .withProperty("do-not-talk-about", "Fight Club")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, "")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    val details = result.details["properties"] as Map<String, String>
    assertThat(details)
      .isNotNull
      .hasSize(2)
      .contains(
        entry("do-not-talk-about", "Fight Club"),
        entry("FirstRule", "You do not talk about Fight Club")
      )
  }

  @Test
  fun `contribute should obfuscate values when password in properties using default pattern`() {
    //Arrange
    val mockEnv = MockEnvironment()
      .withProperty("login", "Tyler_Durden")
      .withProperty("password", "you_do_not_talk_about_Fight_Club")
      .withProperty("real.pwd", "do-not-talk-about_Fight Club")
      .withProperty("fake.passwd", "do-talk-about_Fight Club")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, ".*(?:password|passwd|pwd).*")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    val details = result.details["properties"] as Map<String, String>
    assertThat(details)
      .isNotNull
      .hasSize(4)
      .contains(
        entry("fake.passwd", "******"),
        entry("login", "Tyler_Durden"),
        entry("password", "******"),
        entry("real.pwd", "******")
      )
  }

  @Test
  fun `contribute should obfuscate values when password in properties with custom pattern`() {
    //Arrange
    val mockEnv = MockEnvironment()
      .withProperty("login", "Tyler_Durden")
      .withProperty("password", "you_do_not_talk_about_Fight_Club")
      .withProperty("real.pwd", "do-not-talk-about_Fight Club")
      .withProperty("fake.passwd", "do-talk-about_Fight Club")
    val infoBuilder = Info.Builder()
    val toTest = EnvInfosContributor(mockEnv, ".*login.*")
    toTest.buildPropertyMap()

    //Act
    toTest.contribute(infoBuilder)
    val result = infoBuilder.build()

    //Assert
    val details = result.details["properties"] as Map<String, String>
    assertThat(details)
      .isNotNull
      .hasSize(4)
      .contains(
        entry("fake.passwd", "do-talk-about_Fight Club"),
        entry("login", "******"),
        entry("password", "you_do_not_talk_about_Fight_Club"),
        entry("real.pwd", "do-not-talk-about_Fight Club")
      )
  }
}