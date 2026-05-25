package com.campusenroll.enrollment_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceEndpointsProperties.class)
public class ServiceClientConfig {

    @Bean
    public RestClient studentRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder
                .baseUrl(endpoints.getStudentBaseUrl())
                .requestFactory(buildRequestFactory(endpoints))
                .build();
    }

    @Bean
    public RestClient courseRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder
                .baseUrl(endpoints.getCourseBaseUrl())
                .requestFactory(buildRequestFactory(endpoints))
                .build();
    }

    @Bean
    public RestClient billingRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder
                .baseUrl(endpoints.getBillingBaseUrl())
                .requestFactory(buildRequestFactory(endpoints))
                .build();
    }

    private SimpleClientHttpRequestFactory buildRequestFactory(ServiceEndpointsProperties endpoints) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(endpoints.getConnectTimeoutMs());
        factory.setReadTimeout(endpoints.getReadTimeoutMs());
        return factory;
    }
}
