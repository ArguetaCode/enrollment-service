package com.campusenroll.enrollment_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "campusenroll.services")
public class ServiceEndpointsProperties {

    private String studentBaseUrl;

    private String courseBaseUrl;

    private String billingBaseUrl;
}
