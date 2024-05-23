package de.proeller.applications.employeetest.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.proeller.applications.employeetest.TestUtil;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
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
        CreateEmployeeRequestDto requestDto = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(List.of("Music", "Sports")).build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
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
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(Matchers.containsString("E-Mail Address is already in use")));

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
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(requestDto.getEmail()));
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