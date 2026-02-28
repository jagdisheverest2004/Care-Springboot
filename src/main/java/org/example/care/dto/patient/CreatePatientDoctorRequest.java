package org.example.care.dto.patient;

import lombok.Data;
import org.example.care.dto.drug.AddPatientDrugRequest;

import java.util.List;

@Data
public class CreatePatientDoctorRequest {
    private String purpose;
    private String notes;
    private List<AddPatientDrugRequest> newDrugs;
}
