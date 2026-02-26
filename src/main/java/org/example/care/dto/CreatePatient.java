package org.example.care.dto;

import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class CreatePatient {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String bloodGroup;

    @Column(length = 2000)
    private String chronicConditions;

    private List<String> newMeds;
}
