package com.campusenroll.enrollment_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.campusenroll.enrollment_service.integration.dto.PaymentEvent;
import com.campusenroll.enrollment_service.service.EnrollmentService;
import lombok.SneakyThrows;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final EnrollmentService enrollmentService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${campusenroll.messaging.payment-approved-queue}")
    @SneakyThrows
    public void handlePaymentApproved(Message message) {

        enrollmentService.confirmEnrollmentPayment(
                readPaymentEvent(message)
        );
    }

    @RabbitListener(queues = "${campusenroll.messaging.payment-failed-queue}")
    @SneakyThrows
    public void handlePaymentFailed(Message message) {

        enrollmentService.failEnrollmentPayment(
                readPaymentEvent(message)
        );
    }

    private PaymentEvent readPaymentEvent(Message message)
            throws java.io.IOException {

        return objectMapper.readValue(
                message.getBody(),
                PaymentEvent.class
        );
    }
}
