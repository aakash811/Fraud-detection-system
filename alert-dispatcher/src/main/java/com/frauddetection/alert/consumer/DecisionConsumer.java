package com.frauddetection.alert.consumer;

import com.frauddetection.alert.producer.AlertProducer;
import com.frauddetection.alert.service.NotificationService;
import com.frauddetection.common.event.DecisionEvent;
import com.frauddetection.common.model.Action;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionConsumer {

    private final NotificationService notificationService;
    private final AlertProducer alertProducer;

    @KafkaListener(topics = "decisions.v1", groupId = "alert-dispatcher")
    public void processDecision(DecisionEvent event) {
        if (event.getAction() == Action.BLOCK || event.getAction() == Action.THROTTLE) {
            log.info("High-risk decision detected: {} for card {} - sending alert", 
                event.getAction(), event.getCardLast4());
            notificationService.sendFraudAlert(event);
            alertProducer.sendAlert(event);
        }
    }
}