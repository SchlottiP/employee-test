package de.proeller.applications.employeetest.exception;

import lombok.Getter;

@Getter
public class ValidationErrorResponse {
    private String field;
    private String message;

    public ValidationErrorResponse(String field, String message) {
        this.field = field;
        this.message = message;
    }


}