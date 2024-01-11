package com.ugovia.kafka.kafkademo.producer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ugovia.kafka.kafkademo.client.SnmpGet;
import com.ugovia.kafka.kafkademo.model.EventLog;
import org.slf4j.Logger;

@RestController
@CrossOrigin(origins = "http://192.168.1.104:3000, http://192.168.1.22:3000, http://localhost:3000", maxAge = 3600)
public class KafkaDemoController {

    static final Logger log = LoggerFactory.getLogger(KafkaDemoController.class);

    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public void setKafkaTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/api")
    public void sendMessage(@RequestParam String oid) {
        List<EventLog> logs = SnmpGet.snmpGet("166.165.252.96", "public", oid);

        logs.forEach(element -> {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("sensor-events",
                    "166.165.252.96 >> " + element.getVariable());
            String data = element.getVariable();
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent message=[{}] with offset=[{}]", data, result.getRecordMetadata().offset());
                } else {
                    log.info("Unable to send message=[{}] due to : {}", data, ex.getMessage());
                }
            });
        });
    }

    @MessageMapping("/sendData")
    @SendTo("/topic/group")
    public String broadcastGroupMessage(@Payload String message) {
        // Sending this message to all the subscribers
        return message;
    }
}
