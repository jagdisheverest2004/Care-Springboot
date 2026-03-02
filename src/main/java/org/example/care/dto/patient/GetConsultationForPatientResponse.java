package org.example.care.dto.patient;

import lombok.Data;
import org.example.care.dto.drug.PrescriptionRetreival;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;
import org.example.care.model.enumeration.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetConsultationForPatientResponse {
    private Long id;
    private Long prescribedDoctorId;
    private String prescribedDoctorName;
    private String purpose;
    private String notes;
    private RiskLevel riskLevel;
    private List<MedicalRecordRetreival> medicalRecords;
    private List<PrescriptionRetreival> drugsPrescribed;
    private LocalDateTime visitedAt;
}
