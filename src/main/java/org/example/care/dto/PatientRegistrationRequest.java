package org.example.care.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class PatientRegistrationRequest {

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String bloodGroup;
}
