package de.proeller.applications.employeetest.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.proeller.applications.employeetest.TestUtil;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.kafka.EmployeeEvent;
import de.proeller.applications.employeetest.kafka.EmployeeEventType;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static de.proeller.applications.employeetest.TestUtil.setUpKafkaConsumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EmployeeApplicationE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    private BlockingQueue<ConsumerRecord<String, EmployeeEvent>> records;

    private CountDownLatch latch;


    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        latch = new CountDownLatch(1);
        records = setUpKafkaConsumer(latch);
    }

    @Test
    void testCreateEmployee() throws Exception {
        CreateEmployeeRequestDto requestDto = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();

        MvcResult mvcResult = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()))
                .andExpect(jsonPath("$.fullName").value("John Doe")).andReturn();

        String responseString = mvcResult.getResponse().getContentAsString();
        EmployeeResponseDto createdEmployee = objectMapper.readValue(responseString, EmployeeResponseDto.class);
        UUID createdEmployeeId = createdEmployee.getId();


        mockMvc.perform(get("/api/employees/" + createdEmployeeId)
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdEmployeeId.toString()))
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        }

    @Test
    void testCreateEmployee_mailAlreadyExists() throws Exception {
        Employee alreadyExisting = Employee.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();
        Employee savedExistingEmployee = employeeRepository.save(alreadyExisting);

        CreateEmployeeRequestDto toCreate = CreateEmployeeRequestDto.builder()
                .email(savedExistingEmployee.getEmail())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();


        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate))
                        .with(httpBasic("user", "password")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("E-Mail Address is already in use")));
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isFalse();

    }

    @Test
    void testUpdateEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setEmail(TestUtil.createRandomEmailAddress());
        employee.setFullName("John Doe");
        employee.setBirthday(LocalDate.of(1990, 1, 1));
        employee.setHobbies(List.of("Music", "Sports"));
        Employee savedEmployee = employeeRepository.save(employee);

        UpdateEmployeeRequestDto requestDto = UpdateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress()).build();

        mockMvc.perform(put("/api/employees/" + savedEmployee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()));

    }

    @Test
    void testDeleteEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setEmail(TestUtil.createRandomEmailAddress());
        employee.setFullName("John Doe");
        employee.setBirthday(LocalDate.of(1990, 1, 1));
        employee.setHobbies(List.of("Music", "Sports"));
        Employee savedEmployee = employeeRepository.save(employee);


        mockMvc.perform(delete("/api/employees/" + savedEmployee.getId())
                .with(httpBasic("user", "password")))
                .andExpect(status().isNoContent());


        mockMvc.perform(get("/api/employees/" + savedEmployee.getId())
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNotFound());

    }

    @Test
    void testCreateEmployee_Unauthenticated() throws Exception {
        CreateEmployeeRequestDto requestDto = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isFalse();
    }

    @Test
    void testGetAllEmployees_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
        ConsumerRecord<String, EmployeeEvent> received = records.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNull();
    }

    @Test
    void testGetEmployeeById_Unauthenticated() throws Exception {
        // Authenticates Successfully, id just not in db
        UUID employeeId = UUID.randomUUID();
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isNotFound());
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isFalse();
    }

    @Test
    void testUpdateEmployee_Unauthenticated() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UpdateEmployeeRequestDto requestDto = UpdateEmployeeRequestDto.builder()
                .email("newemail@example.com")
                .fullName("John Updated Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Reading", "Writing")).build();

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isFalse();
    }

    @Test
    void testDeleteEmployee_Unauthenticated() throws Exception {
        UUID employeeId = UUID.randomUUID();
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isUnauthorized());
        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isFalse();
    }


    /**
     * Test is disabled because of the flaky behaviour with a local Kafka.
     * Running it manually independently works.
     */
    @Disabled
    @Test
    void testKafkaEventsSend() throws Exception{
        CreateEmployeeRequestDto createDto = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());

        boolean createReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(createReceived).isTrue();
        ConsumerRecord<String, EmployeeEvent> createEvent = records.poll();
        assertThat(createEvent).isNotNull();
        assertThat(createEvent.value().getEmployeeEventType()).isEqualTo(EmployeeEventType.CREATE);

        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .hobbies(List.of("Music", "Sports")).build();

        mockMvc.perform(put("/api/employees/" + createEvent.value().getEmployee().getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(httpBasic("user", "password")))
                .andExpect(status().isOk());


        boolean updateReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(updateReceived).isTrue();
        ConsumerRecord<String, EmployeeEvent> updateEvent = records.poll();
        assertThat(updateEvent).isNotNull();
        assertThat(updateEvent.value().getEmployeeEventType()).isEqualTo(EmployeeEventType.UPDATE);

        mockMvc.perform(delete("/api/employees/" + updateEvent.value().getEmployee().getId())
                        .with(httpBasic("user", "password")))
                .andExpect(status().isNoContent());

        boolean deleteReceived = latch.await(10, TimeUnit.SECONDS);
        assertThat(deleteReceived).isTrue();
        ConsumerRecord<String, EmployeeEvent> deleteEvent = records.poll();
        assertThat(deleteEvent).isNotNull();
        assertThat(deleteEvent.value().getEmployeeEventType()).isEqualTo(EmployeeEventType.DELETE);
    }

}