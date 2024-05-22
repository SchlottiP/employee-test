package de.proeller.applications.employeetest.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateEmployeeRequestDto {

    @NotNull
    @Email
    private String email;

    @NotNull
    @NotEmpty
    private String fullName;

    @NotNull
    private LocalDate birthday;

    @NotNull
    @NotEmpty
    private List<String> hobbies;
}