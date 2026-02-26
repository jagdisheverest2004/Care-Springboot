package org.example.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePatient {

    private String name;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String chronicConditions;
    private List<String> newMeds;
}
