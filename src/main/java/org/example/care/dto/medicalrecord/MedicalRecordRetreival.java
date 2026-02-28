package org.example.care.dto.medicalrecord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordRetreival {
    private Long recordId;
    private String fileName;
    private String fileType;
    private String fileSummary;
    private LocalDateTime uploadedAt;
}
