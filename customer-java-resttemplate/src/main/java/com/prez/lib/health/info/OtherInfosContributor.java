package com.prez.lib.health.info;

import java.time.OffsetDateTime;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

public class OtherInfosContributor implements InfoContributor {

    private static final OffsetDateTime START_DATE = OffsetDateTime.now();

    private boolean isSnapshot;
    private String applicationType;

    public OtherInfosContributor(boolean isSnapshot, String applicationType) {
        this.isSnapshot = isSnapshot;
        this.applicationType = applicationType;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("snapshot", isSnapshot);
        builder.withDetail("startDate", START_DATE.toString());
        builder.withDetail("applicationType", applicationType);
    }

}