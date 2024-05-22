package de.proeller.applications.employeetest.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.KafkaEvent;
import org.springframework.stereotype.Service;

/**
 * No Unit-Test for this class,
 * because it uses straight forward the kafka template
 */
@Service
public class KafkaProducer {

    @Value("${messaging.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, EmployeeEvent> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, EmployeeEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(EmployeeEvent message) {
        kafkaTemplate.send(topic, message);
    }
}