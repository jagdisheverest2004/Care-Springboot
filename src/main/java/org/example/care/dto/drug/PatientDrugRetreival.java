package org.example.care.dto.drug;

import lombok.Data;
import org.example.care.model.DrugTime;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientDrugRetreival {
    private Long id;
    private Long drugId;
    private String drugName;
    private String dosage;
    private String instructions;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DrugTime> drugTimes; // e.g., "Morning, Afternoon, Evening, Night"
}
