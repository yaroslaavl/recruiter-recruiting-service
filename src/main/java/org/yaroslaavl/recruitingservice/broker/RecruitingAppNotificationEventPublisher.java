package org.yaroslaavl.recruitingservice.broker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaroslaavl.recruitingservice.broker.dto.NotificationDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruitingAppNotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.recruiting.exchange}")
    public String exchange;

    @Value("${rabbitmq.recruiting.queues.start.routing-key}")
    public String routingKey;

    public void publishInAppNotification(NotificationDto notificationDto) {
        rabbitTemplate.convertAndSend(exchange, routingKey, notificationDto);
    }
}
