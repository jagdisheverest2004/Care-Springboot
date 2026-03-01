package org.example.care.dto.consultation;

import lombok.Data;

import java.util.List;

@Data
public class GetConsultationResponses {
    List<GetConsultationResponse> consultations;
}
