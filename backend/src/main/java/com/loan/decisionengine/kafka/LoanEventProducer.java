package com.loan.decisionengine.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LoanEventProducer {

    private static final Logger log = LoggerFactory.getLogger(LoanEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public void sendApplicationSubmitted(LoanApplicationEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.LOAN_APPLICATION_SUBMITTED,
                    event.getApplicationNumber(), event);
            log.debug("Published APPLICATION_SUBMITTED for {}", event.getApplicationNumber());
        } catch (Exception e) {
            log.warn("Failed to publish APPLICATION_SUBMITTED event: {}", e.getMessage());
        }
    }

    @Async
    public void sendDecisionCompleted(LoanApplicationEvent event) {
        try {
            String topic = "MANUAL_REVIEW".equals(event.getDecision())
                    ? KafkaTopics.LOAN_MANUAL_REVIEW : KafkaTopics.LOAN_DECISION_COMPLETED;
            kafkaTemplate.send(topic, event.getApplicationNumber(), event);
            log.debug("Published DECISION_COMPLETED ({}) for {}", event.getDecision(), event.getApplicationNumber());
        } catch (Exception e) {
            log.warn("Failed to publish DECISION_COMPLETED event: {}", e.getMessage());
        }
    }

    @Async
    public void sendRuleUpdate(RuleUpdateEvent event) {
        try {
            kafkaTemplate.send(KafkaTopics.RULE_UPDATE_NOTIFICATION, event.getRuleName(), event);
            log.info("Published RULE_UPDATE for rule '{}'", event.getRuleName());
        } catch (Exception e) {
            log.warn("Failed to publish RULE_UPDATE event: {}", e.getMessage());
        }
    }
}
