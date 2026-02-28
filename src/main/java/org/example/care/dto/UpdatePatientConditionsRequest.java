package org.example.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePatientConditionsRequest {
    private String chronicConditions;
    private CreatePatientDoctorRequest doctorVisit;
}
