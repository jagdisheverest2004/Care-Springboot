package org.example.care.dto;

import org.example.care.model.MedicalRecord;
import org.example.care.model.MedicalRecordType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MedicalRecordResponse {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private MedicalRecordType type;
    private String summary;

    public static MedicalRecordResponse from(MedicalRecord record) {
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .doctorId(record.getDoctorId())
                .type(record.getType())
                .summary(record.getSummary())
                .build();
    }
}
