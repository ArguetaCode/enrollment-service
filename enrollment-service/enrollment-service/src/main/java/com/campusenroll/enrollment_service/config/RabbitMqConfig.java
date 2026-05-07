package com.campusenroll.enrollment_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange paymentsExchange(
            @Value("${campusenroll.messaging.payments-exchange}") String exchangeName
    ) {

        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue paymentApprovedQueue(
            @Value("${campusenroll.messaging.payment-approved-queue}") String queueName
    ) {

        return new Queue(queueName, true);
    }

    @Bean
    public Queue paymentFailedQueue(
            @Value("${campusenroll.messaging.payment-failed-queue}") String queueName
    ) {

        return new Queue(queueName, true);
    }

    @Bean
    public Binding paymentApprovedBinding(
            Queue paymentApprovedQueue,
            DirectExchange paymentsExchange,
            @Value("${campusenroll.messaging.payment-approved-routing-key}") String routingKey
    ) {

        return BindingBuilder.bind(paymentApprovedQueue)
                .to(paymentsExchange)
                .with(routingKey);
    }

    @Bean
    public Binding paymentFailedBinding(
            Queue paymentFailedQueue,
            DirectExchange paymentsExchange,
            @Value("${campusenroll.messaging.payment-failed-routing-key}") String routingKey
    ) {

        return BindingBuilder.bind(paymentFailedQueue)
                .to(paymentsExchange)
                .with(routingKey);
    }

}
