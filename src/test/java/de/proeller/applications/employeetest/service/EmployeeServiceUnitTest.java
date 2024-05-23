package de.proeller.applications.employeetest.service;

import de.proeller.applications.employeetest.TestUtil;
import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.exception.CustomRuntimeException;
import de.proeller.applications.employeetest.kafka.EmployeeEvent;
import de.proeller.applications.employeetest.kafka.MessageProducerService;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import de.proeller.applications.employeetest.service.mapper.EmployeeMapper;
import org.junit.jupiter.api.Nested;
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

/**
 * Methods that don't get a test because the test
 * would only repeat they method and the method is too simple:
 * - getAllEmployees
 * - getEmployeeById
 *
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private MessageProducerService messageProducerService;

    @InjectMocks
    private EmployeeService employeeService;


    @Nested
    class CreateEmployee{
    @Test
    void createNormalUser() {
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
        verify(messageProducerService, times(1)).sendMessage(any(EmployeeEvent.class));
    }

    @Test
    void createSameEmailTwice() {
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


        employeeService.createEmployee(dto1);
        CustomRuntimeException actualException = assertThrows(CustomRuntimeException.class, () -> employeeService.createEmployee(dtoWithSameEmail));
        assertThat(actualException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(employeeRepository, times(1)).save(any());
    }
    }

    @Nested
    class UpdateEmployee{
    @Test
    void allChanged() {
        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();
        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder()
                .birthday(LocalDate.of(2010, 4,3))
                .email(TestUtil.createRandomEmailAddress())
                .fullName("Another Name")
                .hobbies(List.of())
                .build();

        Employee expectedResultEmployee = Employee.builder()
                .id(employee.getId())
                .birthday(LocalDate.of(2010, 4,3))
                .email(updateDto.getEmail())
                .fullName("Another Name")
                .hobbies(List.of())
                .build();

        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.save(expectedResultEmployee)).thenReturn(expectedResultEmployee);
        when(employeeMapper.toResponseDto(expectedResultEmployee)).thenReturn(EmployeeResponseDto.builder().build());

        EmployeeResponseDto responseDto = employeeService.updateEmployee(employee.getId(), updateDto);

        assertNotNull(responseDto);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(messageProducerService, times(1)).sendMessage(any(EmployeeEvent.class));
    }

        @Test
        void nothingChanged() {
            Employee employee = Employee.builder()
                    .id(UUID.randomUUID())
                    .email(TestUtil.createRandomEmailAddress())
                    .fullName("John Doe")
                    .birthday(LocalDate.of(1990, 1, 1))
                    .hobbies(Arrays.asList("Music", "Sports"))
                    .build();
            UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder()
                    .build();


            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(employeeRepository.save(employee)).thenReturn(employee);
            when(employeeMapper.toResponseDto(employee)).thenReturn(EmployeeResponseDto.builder().build());

            EmployeeResponseDto responseDto = employeeService.updateEmployee(employee.getId(), updateDto);

            assertNotNull(responseDto);
            verify(employeeRepository, times(1)).save(any(Employee.class));
            verify(messageProducerService, times(1)).sendMessage(any(EmployeeEvent.class));
        }

    @Test
    void notExisting(){
        UpdateEmployeeRequestDto updateDTO = UpdateEmployeeRequestDto.builder().build();

        when(employeeRepository.findById(any())).thenReturn(Optional.empty());

        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> employeeService.updateEmployee(UUID.randomUUID(), updateDTO));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(employeeRepository, times(0)).save(any(Employee.class));
        verify(messageProducerService, times(0)).sendMessage(any(EmployeeEvent.class));
    }

    @Test
    void EmailAddressConflict(){
        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();
        Employee anotherEmployee = Employee.builder()
                .id(UUID.randomUUID())
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();
        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder()
                .email(anotherEmployee.getEmail())
                .build();


        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.findByEmail(anotherEmployee.getEmail())).thenReturn(Optional.of(anotherEmployee));

        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> employeeService.updateEmployee(employee.getId(), updateDto));
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        verify(employeeRepository, times(0)).save(any(Employee.class));
        verify(messageProducerService, times(0)).sendMessage(any(EmployeeEvent.class));
    }
    }

    @Test
    void sameEmailAddressInUpdate(){
        Employee employee = Employee.builder()
                .id(UUID.randomUUID())
                .email(TestUtil.createRandomEmailAddress())
                .fullName("John Doe")
                .birthday(LocalDate.of(1990, 1, 1))
                .hobbies(Arrays.asList("Music", "Sports"))
                .build();

        UpdateEmployeeRequestDto updateDto = UpdateEmployeeRequestDto.builder()
                .email(employee.getEmail())
                .build();


        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.toResponseDto(employee)).thenReturn(EmployeeResponseDto.builder().build());

        EmployeeResponseDto response = employeeService.updateEmployee(employee.getId(), updateDto);
        assertNotNull(response);

        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(messageProducerService, times(1)).sendMessage(any(EmployeeEvent.class));
    }

    @Nested
    class DeleteEmployee{

        @Test
        void deleteEmployee(){

            UUID id = UUID.randomUUID();
            when(employeeRepository.findById(id)).thenReturn(Optional.ofNullable(Employee.builder().build()));
            employeeService.deleteEmployee(id);
            verify(employeeRepository, times(1)).deleteById(id);
            verify(messageProducerService, times(1)).sendMessage(any(EmployeeEvent.class));
        }
    }
}
