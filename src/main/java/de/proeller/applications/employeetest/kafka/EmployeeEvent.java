package de.proeller.applications.employeetest.kafka;

import de.proeller.applications.employeetest.model.Employee;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EmployeeEvent {
    private Employee employee;
    private EmployeeEventType employeeEventType;
}

