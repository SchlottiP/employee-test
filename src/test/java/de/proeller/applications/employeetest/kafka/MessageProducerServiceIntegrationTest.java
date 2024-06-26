package de.proeller.applications.employeetest.kafka;

import de.proeller.applications.employeetest.model.Employee;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static de.proeller.applications.employeetest.TestUtil.setUpKafkaConsumer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MessageProducerServiceIntegrationTest {

    @Autowired
    private MessageProducerService messageProducerService;

    private BlockingQueue<ConsumerRecord<String, EmployeeEvent>> records;

    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        records = setUpKafkaConsumer(latch);
    }


    @Test
    void testSendMessage() throws InterruptedException {
        Employee employee = new Employee(UUID.randomUUID(), "test@example.com", "John Doe", LocalDate.of(1990, 1, 1), List.of("Music", "Sports"));
        EmployeeEvent event = new EmployeeEvent(employee, EmployeeEventType.CREATE);
        messageProducerService.sendMessage(event);

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();
        ConsumerRecord<String, EmployeeEvent> received = records.poll();
        assertThat(received).isNotNull();
        assertThat(received.value()).isEqualTo(event);
    }
}