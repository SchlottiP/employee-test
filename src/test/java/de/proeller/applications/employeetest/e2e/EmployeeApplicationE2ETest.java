package de.proeller.applications.employeetest.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.proeller.applications.employeetest.TestUtil;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeApplicationE2ETest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void testCreateEmployee() throws Exception {
        CreateEmployeeRequestDto requestDto =  CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void testUpdateEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setEmail(TestUtil.createRandomEmailAddress());
        employee.setFullName("John Doe");
        employee.setBirthday(LocalDate.of(1990, 1, 1));
        employee.setHobbies(List.of("Music", "Sports"));
        employeeRepository.save(employee);

        UpdateEmployeeRequestDto requestDto =  UpdateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress()).build();

        mockMvc.perform(put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void testDeleteEmployee() throws Exception {
        Employee employee = new Employee();
        employee.setId(UUID.randomUUID());
        employee.setEmail(TestUtil.createRandomEmailAddress());
        employee.setFullName("John Doe");
        employee.setBirthday(LocalDate.of(1990, 1, 1));
        employee.setHobbies(List.of("Music", "Sports"));
        employeeRepository.save(employee);


        mockMvc.perform(delete("/api/employees/" + employee.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/employees/" + employee.getId()))
                .andExpect(status().isNotFound());
    }
}