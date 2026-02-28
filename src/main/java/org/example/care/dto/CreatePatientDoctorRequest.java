package org.example.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePatientDoctorRequest {
    private String purpose;
    private String notes;
    private List<AddPatientDrugRequest> newDrugs;
}
