package com.prez.config;

import brave.SpanCustomizer;
import com.prez.interceptors.JsonHeadersInterceptor;
import com.prez.lib.tracing.ResponseMarkerFilter;
import com.prez.lib.tracing.SpanCustomizationWebClientFilter;
import com.prez.ws.CustomerWSClient;
import com.prez.ws.CustomerWSProperties;
import com.prez.ws.handler.CustomerErrorHandler;
import com.prez.ws.handler.PlainTextErrorHandler;
import com.prez.ws.handler.SpanCustomizationWebClientErrorHandler;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.metrics.AutoTimer;
import org.springframework.boot.actuate.metrics.web.client.MetricsRestTemplateCustomizer;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(CustomerWSProperties.class)
public class CustomerWebServiceConfig {

    private final SpanCustomizer spanCustomizer;
    private final MeterRegistry meterRegistry;
    private final RestTemplateExchangeTagsProvider tagsProvider;
    private final DistributionSummary customerWSSummary;

    @Autowired
    public CustomerWebServiceConfig(SpanCustomizer spanCustomizer,
                                    MeterRegistry meterRegistry,
                                    @Qualifier("customerWSSummary") DistributionSummary customerWSSummary,
                                    RestTemplateExchangeTagsProvider tagsProvider) {
        this.spanCustomizer = spanCustomizer;
        this.meterRegistry = meterRegistry;
        this.customerWSSummary = customerWSSummary;
        this.tagsProvider = tagsProvider;
    }

    @Bean
    public CustomerWSClient customerWSClient(CustomerWSProperties customerSourceProperties,
                                             @Qualifier("customerRestTemplate") RestOperations customerRestTemplate) {
        return new CustomerWSClient(customerSourceProperties, customerRestTemplate);
    }

    @Bean(name = "customerRestTemplate")
    public RestOperations customerRestTemplate(CustomerWSProperties webServiceProperties) {
        return createRestTemplate("CustomerWS", webServiceProperties, new CustomerErrorHandler());
    }

    @Bean(name = "customerPreferencesRestTemplate")
    public RestOperations customerPreferencesRestTemplate(CustomerWSProperties webServiceProperties) {
        String name = "customerPreferencesWS";
        return createRestTemplate(name, webServiceProperties, defaultErrorHandler(name));
    }

    private RestTemplate createRestTemplate(String name,
                                            CustomerWSProperties customerWSProperties,
                                            ResponseErrorHandler errorHandler) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(customerWSProperties.getUrl()));
        restTemplate.setInterceptors(defaultInterceptors(name));
        restTemplate.setErrorHandler(new SpanCustomizationWebClientErrorHandler(name, errorHandler, spanCustomizer));
        // In order to have our custom metrics for this REST service
        new MetricsRestTemplateCustomizer(meterRegistry,
                tagsProvider,
                "devoxx." + name.toLowerCase(),
                AutoTimer.ENABLED
        ).customize(restTemplate);
        return restTemplate;
    }

    private List<ClientHttpRequestInterceptor> defaultInterceptors(String name) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new JsonHeadersInterceptor());
        interceptors.add(new SpanCustomizationWebClientFilter(name, spanCustomizer));
        interceptors.add(new ResponseMarkerFilter(customerWSSummary));
        return interceptors;
    }

    private ResponseErrorHandler defaultErrorHandler(String name) {
        return new PlainTextErrorHandler(name);
    }
}
