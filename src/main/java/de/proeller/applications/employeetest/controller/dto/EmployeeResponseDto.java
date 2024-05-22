package de.proeller.applications.employeetest.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EmployeeResponseDto {
    private UUID id;
    private String email;
    private String fullName;
    private LocalDate birthday;
    private List<String> hobbies;
}
