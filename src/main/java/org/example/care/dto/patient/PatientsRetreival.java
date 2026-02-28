package org.example.care.dto.patient;

import lombok.Data;

import java.util.List;

@Data
public class PatientsRetreival {
    List<PatientRetreival> patients;
}
