package com.campusenroll.enrollment_service.integration;

import com.campusenroll.enrollment_service.exception.BusinessException;
import com.campusenroll.enrollment_service.integration.dto.PaymentRequest;
import com.campusenroll.enrollment_service.integration.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class BillingServiceClient {

    private final RestClient billingRestClient;

    public BillingServiceClient(
            @Qualifier("billingRestClient") RestClient billingRestClient
    ) {

        this.billingRestClient = billingRestClient;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            PaymentResponse response = billingRestClient.post()
                    .uri("/payments")
                    .body(request)
                    .retrieve()
                    .body(PaymentResponse.class);
            if (response == null) {
                throw new BusinessException("Billing no devolvio respuesta de pago");
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new BusinessException("Error en billing-service durante el cobro");
        }
    }
}
