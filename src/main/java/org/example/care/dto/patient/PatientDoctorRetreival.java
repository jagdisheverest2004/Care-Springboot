package org.example.care.dto.patient;

import lombok.Data;
import org.example.care.dto.drug.PatientDrugRetreival;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;

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
