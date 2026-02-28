package org.example.care.dto.patient;

import lombok.Data;

@Data
public class UpdatePatientConditionsRequest {
    private String chronicConditions;
    private CreatePatientDoctorRequest doctorVisit;
}
