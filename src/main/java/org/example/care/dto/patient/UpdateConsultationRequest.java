package org.example.care.dto.patient;

import lombok.Data;

@Data
public class UpdateConsultationRequest {
    private String chronicConditions;
    private String allergies;
    private CreateConsultationRequest doctorVisit;
}
