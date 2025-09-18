package org.yaroslaavl.recruitingservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    private final String recruitingExchange;
    private final String recruitingQueue;
    private final String recruitingRoutingKey;
    private final String recruitingDlxExchange;
    private final String recruitingDlqRoutingKey;
    private final long ttl;
    private final long length;
    private final String recruitingDlq;

    public RabbitMqConfig(
            @Value("${rabbitmq.recruiting.exchange}") String recruitingExchange,
            @Value("${rabbitmq.recruiting.queues.start.name}") String recruitingQueue,
            @Value("${rabbitmq.recruiting.queues.start.routing-key}") String recruitingRoutingKey,
            @Value("${rabbitmq.recruiting.queues.start.arguments.x-dead-letter-exchange}") String recruitingDlxExchange,
            @Value("${rabbitmq.recruiting.queues.start.arguments.x-dead-letter-routing-key}") String recruitingDlqRoutingKey,
            @Value("${rabbitmq.recruiting.queues.start.arguments.x-message-ttl}") long ttl,
            @Value("${rabbitmq.recruiting.queues.start.arguments.x-max-length}") long length,
            @Value("${rabbitmq.recruiting.queues.dead-letter.queue.name}") String recruitingDlq
    ) {
        this.recruitingExchange = recruitingExchange;
        this.recruitingQueue = recruitingQueue;
        this.recruitingRoutingKey = recruitingRoutingKey;
        this.recruitingDlxExchange = recruitingDlxExchange;
        this.recruitingDlqRoutingKey = recruitingDlqRoutingKey;
        this.ttl = ttl;
        this.length = length;
        this.recruitingDlq = recruitingDlq;
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(recruitingExchange);
    }

    @Bean
    public DirectExchange userDlx() {
        return new DirectExchange(recruitingDlxExchange);
    }

    @Bean
    public Queue userQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", recruitingDlxExchange);
        args.put("x-dead-letter-routing-key", recruitingDlqRoutingKey);
        args.put("x-message-ttl", ttl);
        args.put("x-max-length", length);
        return  new Queue(recruitingQueue, true, false, false, args);
    }

    @Bean
    public Queue userDlqQueue() {
        Map<String, Object> args = new HashMap<>();
        return  new Queue(recruitingDlq, true, false, false, args);
    }

    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue()).to(userExchange()).with(recruitingRoutingKey);
    }

    @Bean
    public Binding userBindingDlx() {
        return BindingBuilder.bind(userDlqQueue()).to(userDlx()).with(recruitingDlqRoutingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
