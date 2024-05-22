package de.proeller.applications.employeetest.controller.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateEmployeeRequestDto {

    @Email
    private String email;

    private String fullName;

    private LocalDate birthday;

    private List<String> hobbies;
}