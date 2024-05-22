package de.proeller.applications.employeetest.controller.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class UpdateEmployeeRequestDto {

    @Email
    private String email;

    private String fullName;

    private LocalDate birthday;

    private List<String> hobbies;
}