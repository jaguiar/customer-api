package com.prez.lib.health.info;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.Map;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.actuate.info.Info;
import org.springframework.mock.env.MockEnvironment;

public class EnvInfosContributorTest {

  @Test
  @DisplayName("contribute should contribute some env Infos when called")
  public void contribute_shouldContributeSomeEnvInfos_whenCalled() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment()
        .withProperty("Foo", "Bar")
        .withProperty("PIF", "PAF");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, "");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class,
            m -> assertThat(m).containsOnly(entry("Foo", "Bar"), entry("PIF", "PAF")));

  }

  @Test
  @DisplayName("contribute should filter system properties and env when called")
  public void contribute_shouldContributeFilterSystemPropertiesAndEnv_whenCalled() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment();
    System.setProperty("Tchoup", "Tchip");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, "");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class, m -> assertThat(m).isEmpty());

  }

  @Test
  @DisplayName("contribute should resolve properties when called")
  public void contribute_shouldResolveProperties_whenCalled() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment()
        .withProperty("FirstRule", "You do not talk about ${do-not-talk-about}")
        .withProperty("do-not-talk-about", "Fight Club");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, "");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class, m -> assertThat(m)
            .containsOnly(
                entry("do-not-talk-about", "Fight Club"),
                entry("FirstRule", "You do not talk about Fight Club")
            ));
  }

  @Test
  @DisplayName("contribute should handle duplicated values when called")
  public void contribute_shouldHandleDuplicatedValues_whenCalled() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment()
        .withProperty("FirstRule", "${FirstRule}")
        .withProperty("FirstRule", "You do not talk about ${do-not-talk-about}")
        .withProperty("do-not-talk-about", "Fight Club");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, "");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class, m -> assertThat(m)
            .containsOnly(
                entry("do-not-talk-about", "Fight Club"),
                entry("FirstRule", "You do not talk about Fight Club")
            ));
  }

  @Test
  @DisplayName("contribute should obfuscate values when password in properties using default pattern")
  public void contribute_shouldObfuscateValues_whenPasswordInPropertiesWithDefaultPattern() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment()
        .withProperty("login", "Tyler_Durden")
        .withProperty("password", "you_do_not_talk_about_Fight_Club")
        .withProperty("real.pwd", "do-not-talk-about_Fight Club")
        .withProperty("fake.passwd", "do-talk-about_Fight Club");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, ".*(?:password|passwd|pwd).*");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class, m -> assertThat(m)
            .containsOnly(
                entry("fake.passwd", "******"),
                entry("login", "Tyler_Durden"),
                entry("password", "******"),
                entry("real.pwd", "******")
            ));
  }

  @Test
  @DisplayName("contribute should obfuscate values when password in properties with custom pattern")
  public void contribute_shouldObfuscateValues_whenPasswordInPropertiesWithCustomPattern() {
    //Arrange
    final MockEnvironment mockEnv = new MockEnvironment()
        .withProperty("login", "Tyler_Durden")
        .withProperty("password", "you_do_not_talk_about_Fight_Club")
        .withProperty("real.pwd", "do-not-talk-about_Fight Club")
        .withProperty("fake.passwd", "do-talk-about_Fight Club");
    final Info.Builder infoBuilder = new Info.Builder();
    final EnvInfosContributor toTest = new EnvInfosContributor(mockEnv, ".*login.*");
    toTest.buildPropertyMap();

    //Act
    toTest.contribute(infoBuilder);
    final Info result = infoBuilder.build();

    //Assert
    assertThat(result.getDetails().get("properties"))
        .isNotNull()
        .isInstanceOfSatisfying(Map.class, m -> assertThat(m)
            .containsOnly(
                entry("fake.passwd", "do-talk-about_Fight Club"),
                entry("login", "******"),
                entry("password", "you_do_not_talk_about_Fight_Club"),
                entry("real.pwd", "do-not-talk-about_Fight Club")
            ));
  }
}