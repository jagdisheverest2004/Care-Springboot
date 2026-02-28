package org.example.care.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatientDoctorRetreival {
    private Long id;
    private Long prescribedDoctorId;
    private String prescribedDoctorName;
    private String purpose;
    private String notes;
    private List<MedicalRecordRetreival> medicalRecords;
    private List<PatientDrugRetreival> drugsPrescribed;
    private LocalDateTime visitedAt;
}
