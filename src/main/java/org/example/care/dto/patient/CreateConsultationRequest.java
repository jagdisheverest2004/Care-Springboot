package org.example.care.dto.patient;

import lombok.Data;
import org.example.care.dto.drug.AddPrescriptionRequest;
import org.example.care.model.enumeration.RiskLevel;

import java.util.List;

@Data
public class CreateConsultationRequest {
    private String purpose;
    private String notes;
    private RiskLevel riskLevel;
    private List<AddPrescriptionRequest> newDrugs;
}
