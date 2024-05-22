package de.proeller.applications.employeetest.service;

import de.proeller.applications.employeetest.TestUtil;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.exception.CustomRuntimeException;
import de.proeller.applications.employeetest.kafka.KafkaProducer;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import de.proeller.applications.employeetest.service.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private KafkaProducer kafkaProducer;

    @InjectMocks
    private EmployeeService employeeService;



    @Test
    void testCreateEmployee() {
        CreateEmployeeRequestDto dto = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();

        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .birthday(dto.getBirthday())
                .hobbies(dto.getHobbies())
                .build();

        when(employeeMapper.toEntity(dto)).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeRepository.findByEmail(employee.getEmail())).thenReturn(Optional.empty());
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(EmployeeResponseDto.builder().build());


        EmployeeResponseDto responseDto = employeeService.createEmployee(dto);

        assertNotNull(responseDto);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(kafkaProducer, times(1)).sendMessage(any(String.class));
    }

    @Test
    void testCreateSameEmailTwice() {
        CreateEmployeeRequestDto dto1 = CreateEmployeeRequestDto.builder()
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();
        CreateEmployeeRequestDto dtoWithSameEmail = CreateEmployeeRequestDto.builder()
                .email(dto1.getEmail())
                .fullName("Another Name")
                .birthday(LocalDate.of(1990, 2, 5))
                .hobbies(List.of())
                .build();

        Employee employee1 = Employee.builder()
                .id(UUID.randomUUID())
                .email(dto1.getEmail())
                .fullName(dto1.getFullName())
                .birthday(dto1.getBirthday())
                .hobbies(dto1.getHobbies())
                .build();
        Employee employeeWithSameEmail = Employee.builder()
                .id(UUID.randomUUID())
                .email(dtoWithSameEmail.getEmail())
                .fullName(dtoWithSameEmail.getFullName())
                .birthday(dtoWithSameEmail.getBirthday())
                .hobbies(dtoWithSameEmail.getHobbies())
                .build();

        when(employeeMapper.toEntity(dto1)).thenReturn(employee1);
        when(employeeMapper.toEntity(dtoWithSameEmail)).thenReturn(employeeWithSameEmail);
        when(employeeRepository.save(employee1)).thenReturn(employee1);
        when(employeeRepository.findByEmail(employee1.getEmail())).thenReturn(Optional.empty(), Optional.of(employee1));
        when(employeeMapper.toResponseDto(any(Employee.class))).thenReturn(EmployeeResponseDto.builder().build());


        EmployeeResponseDto responseDto = employeeService.createEmployee(dto1);
        CustomRuntimeException actualException = assertThrows(CustomRuntimeException.class, () -> employeeService.createEmployee(dtoWithSameEmail));
        assertThat(actualException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

}