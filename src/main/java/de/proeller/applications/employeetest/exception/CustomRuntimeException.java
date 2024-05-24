package de.proeller.applications.employeetest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class CustomRuntimeException extends RuntimeException {

    private final String userMessage;
    private final HttpStatusCode statusCode;

    public CustomRuntimeException(HttpStatusCode statusCode, String logMessage, String userMessage) {
        super(logMessage);
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }


}