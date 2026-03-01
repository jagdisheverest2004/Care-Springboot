package org.example.care.dto.auth;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRegistrationRequest {

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String bloodGroup;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private LocalDate dateOfBirth;
}
