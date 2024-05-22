package de.proeller.applications.employeetest.service;

import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.exception.CustomRuntimeException;
import de.proeller.applications.employeetest.kafka.EmployeeEvent;
import de.proeller.applications.employeetest.kafka.EmployeeEventType;
import de.proeller.applications.employeetest.kafka.KafkaProducer;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import de.proeller.applications.employeetest.service.mapper.EmployeeMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    private final KafkaProducer kafkaProducer;

    private final EmployeeMapper employeeMapper;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, KafkaProducer kafkaProducer, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.kafkaProducer = kafkaProducer;
        this.employeeMapper = employeeMapper;
    }

    @Transactional
    public EmployeeResponseDto createEmployee(CreateEmployeeRequestDto toCreate) {
        Employee employeeEntityToSave = employeeMapper.toEntity(toCreate);
        validateIfEmployeeMailAlreadyExists(employeeEntityToSave);
        Employee savedEmployee = employeeRepository.save(employeeEntityToSave);
        kafkaProducer.sendMessage(EmployeeEvent.builder().employeeEventType(EmployeeEventType.CREATE).employee(savedEmployee).build());
        return employeeMapper.toResponseDto(savedEmployee);
    }

    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::toResponseDto).toList();
    }

    public Optional<EmployeeResponseDto> getEmployeeById(UUID id) {
        return employeeRepository.findById(id).map(employeeMapper::toResponseDto);
    }

    @Transactional
    public EmployeeResponseDto updateEmployee(UUID id, UpdateEmployeeRequestDto employeeDetails) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new CustomRuntimeException(HttpStatus.NOT_FOUND, "No Employee with id %s found".formatted(id), "Employee is not existing"));
        if (employeeDetails.getEmail() != null && !employeeDetails.getEmail().equals(employee.getEmail())) {
            employee.setEmail(employeeDetails.getEmail());
            validateIfEmployeeMailAlreadyExists(employee);
        }
        if (employeeDetails.getFullName() != null) {
            employee.setFullName(employeeDetails.getFullName());
        }
        if (employeeDetails.getBirthday() != null) {
            employee.setBirthday(employeeDetails.getBirthday());
        }
        if (employeeDetails.getHobbies() != null) {
            employee.setHobbies(employeeDetails.getHobbies());
        }
        Employee updatedEmployee = employeeRepository.save(employee);
        kafkaProducer.sendMessage(EmployeeEvent.builder().employeeEventType(EmployeeEventType.UPDATE).employee(updatedEmployee).build());
        return employeeMapper.toResponseDto(updatedEmployee);
    }

    @Transactional
    public void deleteEmployee(UUID id) {
        Optional<Employee> employeeToDelete = employeeRepository.findById(id);
        if(employeeToDelete.isPresent()) {
            employeeRepository.deleteById(id);
            kafkaProducer.sendMessage(EmployeeEvent.builder().employeeEventType(EmployeeEventType.UPDATE).employee(employeeToDelete.get()).build());
        }
    }
    private void validateIfEmployeeMailAlreadyExists(Employee employee) {
        Optional<Employee> alreadyExisting = employeeRepository.findByEmail(employee.getEmail());
        if(alreadyExisting.isPresent()){
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "Email-Address is already used by user with id " + alreadyExisting.get().getId(), "E-Mail Address is already in use.");
        }
    }
}