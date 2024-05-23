package de.proeller.applications.employeetest.kafka;

import de.proeller.applications.employeetest.model.Employee;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static de.proeller.applications.employeetest.TestUtil.setUpKafkaConsumer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MessageProducerServiceIntegrationTest {

    @Autowired
    private MessageProducerService messageProducerService;

    private BlockingQueue<ConsumerRecord<String, EmployeeEvent>> records;


    @BeforeEach
    void setUp() {
        records = setUpKafkaConsumer();
    }


    @Test
    void testSendMessage() throws InterruptedException {
        Employee employee = new Employee(UUID.randomUUID(), "test@example.com", "John Doe", LocalDate.of(1990, 1, 1), List.of("Music", "Sports"));
        EmployeeEvent event = new EmployeeEvent(employee, EmployeeEventType.CREATE);
        messageProducerService.sendMessage(event);

        ConsumerRecord<String, EmployeeEvent> received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.value()).isEqualTo(event);
    }
}