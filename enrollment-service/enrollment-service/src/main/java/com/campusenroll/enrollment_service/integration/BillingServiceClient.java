package com.campusenroll.enrollment_service.integration;

import com.campusenroll.enrollment_service.integration.dto.PaymentRequest;
import com.campusenroll.enrollment_service.integration.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BillingServiceClient {

    private final RestClient billingRestClient;

    public BillingServiceClient(
            @Qualifier("billingRestClient") RestClient billingRestClient
    ) {

        this.billingRestClient = billingRestClient;
    }

    public PaymentResponse processPayment(PaymentRequest request) {

        return billingRestClient.post()
                .uri("/payments")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
    }
}
