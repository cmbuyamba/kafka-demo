package com.ugovia.kafka.kafkademo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDemoConsumer {

    private static Logger log = LoggerFactory.getLogger(KafkaDemoConsumer.class);
    SimpMessagingTemplate template;

    @Autowired
    public void setTemplate(SimpMessagingTemplate template) {
        this.template = template;
    }

    @KafkaListener(topics = "sensor-events")
    public void listenSnmpMessages(String message) {
        log.info("Received Message in topic sensor-events: {}", message);
        template.convertAndSend("/topic/snmp-messages", message);
    }
}
