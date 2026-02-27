package org.example.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePatient {

    private String chronicConditions;
    private List<String> newMeds;
}
