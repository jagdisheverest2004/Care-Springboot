package org.example.care.dto.consultation;

import lombok.Data;
import org.example.care.model.enumeration.RiskLevel;

import java.time.LocalDateTime;

@Data
public class GetConsultationResponse {
    private Long consultationId;
    private Long patientId;
    private String patientName;
    private String purpose;
    private String notes;
    private LocalDateTime visitedAt;
    private RiskLevel riskLevel;
}
