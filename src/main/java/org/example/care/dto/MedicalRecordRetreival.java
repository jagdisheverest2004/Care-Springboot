package org.example.care.dto;

import lombok.Data;

@Data
public class MedicalRecordRetreival {
    private Long recordId;
    private String fileName;
    private String fileType;
    private String fileSummary;
}
