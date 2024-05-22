package de.proeller.applications.employeetest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class CustomRuntimeException extends RuntimeException {

    @Getter
    private final String userMessage;
    private final HttpStatusCode statusCode;

    public CustomRuntimeException(String logMessage, String userMessage) {
        super(logMessage);
        this.userMessage = userMessage;
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
    public CustomRuntimeException(HttpStatusCode statusCode, String logMessage, String userMessage) {
        super(logMessage);
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }


    public HttpStatusCode getStatusCode(){
        return statusCode;
    }
}