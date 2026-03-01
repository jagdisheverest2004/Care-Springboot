package org.example.care.dto.patient;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PatientRetreival {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String bloodGroup;
    private String contactNumber;
    private String address;
    private String chronicConditions;
    private String allergies;
    private List<GetConsultationForPatientResponse> doctorVisits;
}
