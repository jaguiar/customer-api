package com.prez.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.prez.UsingMongoDB;
import com.prez.model.CustomerPreferences;
import com.prez.model.SeatPreference;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;

@Tag("docker")
@DataMongoTest
class CustomerPreferencesRepositoryTest extends UsingMongoDB {
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private CustomerPreferencesRepository toTest;

    @AfterEach
    void after() {
        mongoOperations.dropCollection(CustomerPreferences.class);
    }

    @Test
    void should_create_new_customer_preferences_if_it_does_not_exist() {
        // Given an empty collection
        CustomerPreferences toCreate = CustomerPreferences.builder()
                .customerId("customerId")
                .classPreference(2)
                .seatPreference(SeatPreference.NO_PREFERENCE)
                .profileName("customProfile")
                .build();

        // When
        CustomerPreferences created = toTest.save(toCreate);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
    }

    @Test
    void should_update_customer_preferences_if_it_exists() {
        // Given
        CustomerPreferences existing = CustomerPreferences.builder()
                .id(UUID.randomUUID().toString())
                .customerId("existing")
                .classPreference(2)
                .seatPreference(SeatPreference.NO_PREFERENCE)
                .profileName("customProfile")
                .build();
        mongoOperations.save(existing);

        CustomerPreferences toUpdate = existing.copy()
                .withSeatPreference(SeatPreference.NEAR_WINDOW);

        // When
        CustomerPreferences updated = toTest.save(toUpdate);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getCustomerId()).isEqualTo("existing");
        assertThat(updated.getSeatPreference()).isEqualTo(SeatPreference.NEAR_WINDOW);
    }

    @Test
    void should_find_customer_preferences_if_it_exists() {
        // Given
        String existingId = UUID.randomUUID().toString();
        CustomerPreferences existing = CustomerPreferences.builder()
                .id(existingId)
                .customerId("existing")
                .classPreference(2)
                .seatPreference(SeatPreference.NO_PREFERENCE)
                .profileName("customProfile")
                .build();
        mongoOperations.save(existing);

        // When
        CustomerPreferences updated = toTest.findById(existingId).orElse(null);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getCustomerId()).isEqualTo("existing");
        assertThat(updated.getSeatPreference()).isEqualTo(SeatPreference.NO_PREFERENCE);
        assertThat(updated.getProfileName()).isEqualTo("customProfile");
    }

    @Test
    void should_not_find_customer_preferences_if_none_exists() {
        // Given an empty collection

        // When
        Optional<CustomerPreferences> notFound = toTest.findById("existing");

        // Then
        assertThat(notFound.isPresent()).isFalse();
    }

    @Test
    void should_find_all_existing_preferences_for_a_given_customer() {
        // Given
        CustomerPreferences defaultProfile = CustomerPreferences.builder()
            .customerId("existing")
            .classPreference(2)
            .seatPreference(SeatPreference.NO_PREFERENCE)
            .profileName("defaultProfile")
            .build();
        CustomerPreferences fancyProfile = CustomerPreferences.builder()
            .customerId("existing")
            .classPreference(1)
            .seatPreference(SeatPreference.NEAR_WINDOW)
            .profileName("fancyProfile")
            .build();
        mongoOperations.save(defaultProfile);
        mongoOperations.save(fancyProfile);

        // When
        List<CustomerPreferences> preferences = toTest.findByCustomerId("existing");

        // Then
        assertThat(preferences).isNotNull();
        assertThat(preferences.size()).isEqualTo(2);
        assertThat(preferences.get(0).getCustomerId()).isEqualTo("existing");
        assertThat(preferences.get(0).getSeatPreference()).isEqualTo(SeatPreference.NO_PREFERENCE);
        assertThat(preferences.get(0).getProfileName()).isEqualTo("defaultProfile");
        assertThat(preferences.get(1).getCustomerId()).isEqualTo("existing");
        assertThat(preferences.get(1).getSeatPreference()).isEqualTo(SeatPreference.NEAR_WINDOW);
        assertThat(preferences.get(1).getProfileName()).isEqualTo("fancyProfile");
    }
}