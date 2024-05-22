package de.proeller.applications.employeetest.service;

import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.exception.CustomRuntimeException;
import de.proeller.applications.employeetest.kafka.KafkaProducer;
import de.proeller.applications.employeetest.model.Employee;
import de.proeller.applications.employeetest.repository.EmployeeRepository;
import de.proeller.applications.employeetest.service.mapper.EmployeeMapper;
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

    public EmployeeResponseDto createEmployee(CreateEmployeeRequestDto toCreate) {
        Employee employeeEntityToSave = employeeMapper.toEntity(toCreate);
        validateIfEmployeeMailAlreadyExists(employeeEntityToSave);
        Employee savedEmployee = employeeRepository.save(employeeEntityToSave);
        kafkaProducer.sendMessage("Employee created: " + savedEmployee.getId());
        return employeeMapper.toResponseDto(savedEmployee);
    }

    private void validateIfEmployeeMailAlreadyExists(Employee employee) {
        Optional<Employee> alreadyExisting = employeeRepository.findByEmail(employee.getEmail());
        if(alreadyExisting.isPresent()){
            throw new CustomRuntimeException(HttpStatus.CONFLICT, "Email-Address is already used by user with id " + alreadyExisting.get().getId(), "E-Mail Address is already in use.");
        }
    }

    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll().stream().map(employeeMapper::toResponseDto).toList();
    }

    public Optional<EmployeeResponseDto> getEmployeeById(UUID id) {
        return employeeRepository.findById(id).map(employeeMapper::toResponseDto);
    }

    public EmployeeResponseDto updateEmployee(UUID id, UpdateEmployeeRequestDto employeeDetails) {
        Employee employee = employeeRepository.findById(id).orElseThrow();
        if (employeeDetails.getEmail() != null) {
            employee.setEmail(employeeDetails.getEmail());
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
        kafkaProducer.sendMessage("Employee updated: " + updatedEmployee.getId());
        return employeeMapper.toResponseDto(updatedEmployee);
    }

    public void deleteEmployee(UUID id) {
        employeeRepository.deleteById(id);
        kafkaProducer.sendMessage("Employee deleted: " + id);
    }
}