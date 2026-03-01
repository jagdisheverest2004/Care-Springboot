package org.example.care.dto.patient;

import lombok.Data;
import org.example.care.dto.drug.AddPatientDrugRequest;
import org.example.care.model.enumeration.RiskLevel;

import java.util.List;

@Data
public class CreatePatientDoctorRequest {
    private String purpose;
    private String notes;
    private RiskLevel riskLevel;
    private List<AddPatientDrugRequest> newDrugs;
}
