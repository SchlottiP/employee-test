package de.proeller.applications.employeetest.service.mapper;

import de.proeller.applications.employeetest.controller.dto.CreateEmployeeRequestDto;
import de.proeller.applications.employeetest.controller.dto.EmployeeResponseDto;
import de.proeller.applications.employeetest.controller.dto.UpdateEmployeeRequestDto;
import de.proeller.applications.employeetest.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public Employee toEntity(CreateEmployeeRequestDto dto) {
        return Employee.builder()
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .birthday(dto.getBirthday())
                .hobbies(dto.getHobbies())
                .build();
    }

    public EmployeeResponseDto toResponseDto(Employee employee) {
        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .fullName(employee.getFullName())
                .birthday(employee.getBirthday())
                .hobbies(employee.getHobbies())
                .build();
    }
}
