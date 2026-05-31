package com.campusenroll.enrollment_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange deadLetterExchange(
            @Value("${campusenroll.messaging.dead-letter-exchange}") String exchangeName
    ) {

        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange paymentsExchange(
            @Value("${campusenroll.messaging.payments-exchange}") String exchangeName
    ) {

        return new DirectExchange(exchangeName);
    }

    @Bean
    public Queue paymentApprovedQueue(
            @Value("${campusenroll.messaging.payment-approved-queue}") String queueName,
            @Value("${campusenroll.messaging.dead-letter-exchange}") String deadLetterExchange,
            @Value("${campusenroll.messaging.payment-approved-dlq}") String dlqName
    ) {

        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", dlqName)
                .build();
    }

    @Bean
    public Queue paymentFailedQueue(
            @Value("${campusenroll.messaging.payment-failed-queue}") String queueName,
            @Value("${campusenroll.messaging.dead-letter-exchange}") String deadLetterExchange,
            @Value("${campusenroll.messaging.payment-failed-dlq}") String dlqName
    ) {

        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", dlqName)
                .build();
    }

    @Bean
    public Queue paymentApprovedDlq(
            @Value("${campusenroll.messaging.payment-approved-dlq}") String queueName
    ) {

        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Queue paymentFailedDlq(
            @Value("${campusenroll.messaging.payment-failed-dlq}") String queueName
    ) {

        return QueueBuilder.durable(queueName).build();
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

    @Bean
    public Binding paymentApprovedDlqBinding(
            Queue paymentApprovedDlq,
            DirectExchange deadLetterExchange,
            @Value("${campusenroll.messaging.payment-approved-dlq}") String routingKey
    ) {

        return BindingBuilder.bind(paymentApprovedDlq)
                .to(deadLetterExchange)
                .with(routingKey);
    }

    @Bean
    public Binding paymentFailedDlqBinding(
            Queue paymentFailedDlq,
            DirectExchange deadLetterExchange,
            @Value("${campusenroll.messaging.payment-failed-dlq}") String routingKey
    ) {

        return BindingBuilder.bind(paymentFailedDlq)
                .to(deadLetterExchange)
                .with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        return factory;
    }

}
