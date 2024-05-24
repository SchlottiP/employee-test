package de.proeller.applications.employeetest.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.exception.GlobalExceptionHandler;
import de.proeller.applications.employeetest.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createEmployee() throws Exception {
        CreateEmployeeRequestDto requestDto = CreateEmployeeRequestDto.builder()
                .email("test@example.com")
                .fullName("John Doe")
                .birthday(LocalDate.parse("1990-01-01"))
                .hobbies(Arrays.asList("Reading", "Hiking")).build();

        EmployeeResponseDto responseDto = EmployeeResponseDto.builder()
                .id(UUID.randomUUID()).email("test@example.com")
                .fullName("John Doe")
                .birthday(LocalDate.parse("1990-01-01"))
                .hobbies(Arrays.asList("Reading", "Hiking")).build();

        given(employeeService.createEmployee(any(CreateEmployeeRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void employeeById() throws Exception {
        UUID employeeId = UUID.randomUUID();
        EmployeeResponseDto responseDto = EmployeeResponseDto.builder().id(employeeId)
                .email("test@example.com")
                .fullName("John Doe").build();

        given(employeeService.getEmployeeById(employeeId)).willReturn(Optional.of(responseDto));

        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void updateEmployee() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder().
                email("update@example.com")
                .fullName("Jane Doe").build();

        EmployeeResponseDto responseDto = EmployeeResponseDto.builder()
                .id(employeeId)
                .email(updateDto.getEmail())
                .fullName(updateDto.getFullName()).build();

        given(employeeService.updateEmployee(eq(employeeId), any(UpdateEmployeeRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("update@example.com"));
    }

    @Test
    void updateEmployee_invalidEmail() throws Exception {
        UUID employeeId = UUID.randomUUID();
        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder().
                email("InValid")
                .fullName("Jane Doe").build();


        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
        }


    @Test
    void deleteEmployee() throws Exception {
        UUID employeeId = UUID.randomUUID();

        willDoNothing().given(employeeService).deleteEmployee(employeeId);

        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNoContent());
    }
}