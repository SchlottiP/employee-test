package de.proeller.applications.employeetest.kafka;

import de.proeller.applications.employeetest.model.Employee;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeEvent {
    private Employee employee;
    private EmployeeEventType employeeEventType;
}

