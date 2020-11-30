package com.prez.lib.health.info;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class EnvInfosContributor implements InfoContributor {


    private static final String OBFUSCATED = "******";
    private final Pattern sensiblePropsNamePattern;
    private ConfigurableEnvironment environment;
    private Map<String, Object> propertyMap;

    public EnvInfosContributor(ConfigurableEnvironment environment, String sensiblePropsNameRegex) {
        this.environment = environment;
        this.sensiblePropsNamePattern = Pattern.compile(sensiblePropsNameRegex);
    }

    @PostConstruct
    void buildPropertyMap() {
        propertyMap = filterSystemProperties();//to keep Map State
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("properties", propertyMap);
    }

    private Map<String, Object> filterSystemProperties() {
        return environment.getPropertySources().stream()
                .filter(MapPropertySource.class::isInstance)
                .filter(propertySource -> !"systemProperties".equals(propertySource.getName()) && !"systemEnvironment".equals(propertySource.getName()))
                .map(MapPropertySource.class::cast)
                .flatMap(mapPropertySource -> mapPropertySource.getSource().keySet().stream())
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::obfuscateSensiblesProps));
    }

    private String obfuscateSensiblesProps(String key) {
        if (sensiblePropsNamePattern.matcher(key.toLowerCase()).matches()) {
            return OBFUSCATED;
        } else {
            return environment.getProperty(key);
        }
    }

}