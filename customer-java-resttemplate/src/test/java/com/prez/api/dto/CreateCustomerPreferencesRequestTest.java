package com.prez.api.dto;


import static com.prez.model.SeatPreference.NEAR_CORRIDOR;
import static com.prez.model.SeatPreference.NEAR_WINDOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateCustomerPreferencesRequestTest {

  private static Validator validator;

  @BeforeAll
  static void beforeAll() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void seatPreference_should_NotBeNull() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(null)
        .classPreference(1)
        .profileName("whatAProfile")
        .language("fr")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The seat preference is missing");
  }



  @Test
  void classPreference_should_NotBeNull() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(null)
        .profileName("whatAProfile")
        .language("es")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The class preference is missing");
  }

  @Test
  void classPreference_should_NotBeLessThan1() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_WINDOW)
        .classPreference(0)
        .profileName("whatAProfile")
        .language("es")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Min value for class preference is 1");
  }

  @Test
  void classPreference_should_NotBeMoreThan2() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(3)
        .profileName("whatAProfile")
        .language("it")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Max value for class preference is 2");
  }

  @Test
  void profileName_should_NotBeNull() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName(null)
        .language("pt")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The profile name is missing");
  }



  @Test
  void profile_should_BeLongerThan1Characters() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("")
        .language("pt")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The profile name should have a size between 1 and 50 characters");
  }

  @Test
  void profile_should_BeShorterThan50Characters() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("blablablablablablablablablbalbalbalbalblablablablablablablablablablablablablablablbalbalbalbalbalbalbalablablbalbalbalbalbalbalbalbalbalbalbalbalbalbalblabalbalbalbablablablablablablabla")
        .language("pt")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The profile name should have a size between 1 and 50 characters");
  }

  @Test
  void profile_should_RespectPattern() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("??What-a-profile??")
        .language("pt")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The profile name contains forbidden characters");
  }

  @Test
  void profileName_could_HaveSmallLetters() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("abcdefghijklmnop")
        .language("fr")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(0);
  }

  @Test
  void profileName_could_HaveCapitalLetters() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        .language("es")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(0);
  }

  @Test
  void profileName_could_HaveSomeOtherCharaters() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("éèêôöïä- ")
        .language("es")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(0);
  }

  @Test
  void profileName_could_notHaveNumbers() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("1234567890")
        .language("es")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("The profile name contains forbidden characters");
  }

  @Test
  void language_could_BeNull() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("what-a-profile")
        .language(null)
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(0);
  }

  @Test
  void language_should_BeOneOfTheSupportedLanguages() {
    // Given
    CreateCustomerPreferencesRequest request = CreateCustomerPreferencesRequest
        .builder()
        .seatPreference(NEAR_CORRIDOR)
        .classPreference(2)
        .profileName("what-a-profile")
        .language("bleh")
        .build();

    // When
    Set<ConstraintViolation<CreateCustomerPreferencesRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isNotNull();
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.iterator().next().getMessage())
        .isEqualTo("The language is not valid. Accepted languages are : fr,de,es,en,it,pt");
  }
}
