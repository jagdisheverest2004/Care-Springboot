package org.example.care.dto.medicalrecord;

import org.example.care.model.MedicalRecord;
import org.example.care.model.enumeration.MedicalRecordType;
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
                .patientId(record.getPatient().getId())
                .doctorId(record.getDoctor().getId())
                .type(record.getType())
                .summary(record.getSummary())
                .build();
    }
}
