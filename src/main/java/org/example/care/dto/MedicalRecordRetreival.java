package org.example.care.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRecordRetreival {
    private Long recordId;
    private String fileName;
    private String fileType;
    private String fileSummary;
    private Long doctorId;
    private String doctorName;
}
