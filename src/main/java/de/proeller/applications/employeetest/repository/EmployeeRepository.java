package de.proeller.applications.employeetest.repository;

import de.proeller.applications.employeetest.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * No Test for this because it's such a basic usage of the JpaRepository.
 */
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);
}