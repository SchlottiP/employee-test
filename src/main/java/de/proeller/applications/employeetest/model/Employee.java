package de.proeller.applications.employeetest.model;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Employee {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private LocalDate birthday;

    @ElementCollection
    private List<String> hobbies;
}