package com.campusenroll.enrollment_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceEndpointsProperties.class)
public class ServiceClientConfig {

    @Bean
    public RestClient studentRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder.baseUrl(endpoints.getStudentBaseUrl()).build();
    }

    @Bean
    public RestClient courseRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder.baseUrl(endpoints.getCourseBaseUrl()).build();
    }

    @Bean
    public RestClient billingRestClient(
            RestClient.Builder builder,
            ServiceEndpointsProperties endpoints
    ) {

        return builder.baseUrl(endpoints.getBillingBaseUrl()).build();
    }
}
