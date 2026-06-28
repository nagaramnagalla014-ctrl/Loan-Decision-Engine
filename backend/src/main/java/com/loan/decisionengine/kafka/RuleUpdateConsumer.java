package com.loan.decisionengine.kafka;

import com.loan.decisionengine.drools.DroolsRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RuleUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(RuleUpdateConsumer.class);

    @Autowired
    private DroolsRuleService droolsRuleService;

    @KafkaListener(topics = KafkaTopics.RULE_UPDATE_NOTIFICATION,
                   groupId = "#{T(java.util.UUID).randomUUID().toString()}")
    public void onRuleUpdate(RuleUpdateEvent event) {
        log.info("Received rule update notification: action={}, rule={}, by={}",
                event.getAction(), event.getRuleName(), event.getUpdatedBy());
        try {
            droolsRuleService.refreshContainer();
            log.info("Drools engine refreshed after rule update");
        } catch (Exception e) {
            log.error("Failed to refresh Drools engine after rule update: {}", e.getMessage());
        }
    }
}
