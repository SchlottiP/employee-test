package de.proeller.applications.employeetest.repository;

import de.proeller.applications.employeetest.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    // Additional query method to find by email
    Employee findByEmail(String email);
}