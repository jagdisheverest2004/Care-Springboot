package org.example.care.dto.drug;

import lombok.Data;
import org.example.care.model.enumeration.DrugTime;

import java.time.LocalDate;
import java.util.List;

@Data
public class AddPrescriptionRequest {
    private Long drugId;
    private String dosage;
    private String instructions;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DrugTime> drugTimes; // e.g., ["Morning", "Afternoon", "Evening"]
}
