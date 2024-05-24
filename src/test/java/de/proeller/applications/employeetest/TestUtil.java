package de.proeller.applications.employeetest;

import de.proeller.applications.employeetest.kafka.EmployeeEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class TestUtil {
    public static String createRandomEmailAddress(){
        return RandomStringUtils.randomAlphabetic(5)+ "@" + RandomStringUtils.randomAlphabetic(6) + ".com";
    }

    public static BlockingQueue<ConsumerRecord<String, EmployeeEvent>> setUpKafkaConsumer(CountDownLatch latch) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "employee-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class.getName());
        consumerProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class.getName());
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "de.proeller.applications.employeetest.kafka.EmployeeEvent");
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, EmployeeEvent> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties("employee-topic");
        ConcurrentMessageListenerContainer<String, EmployeeEvent> container = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        BlockingQueue<ConsumerRecord<String, EmployeeEvent>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, EmployeeEvent>) record -> {
            records.add(record);
            latch.countDown();
        });
        container.start();
        return records;
    }
}
