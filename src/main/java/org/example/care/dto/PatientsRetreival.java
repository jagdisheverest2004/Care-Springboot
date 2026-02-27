package org.example.care.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatientsRetreival {
    List<PatientRetreival> patients;
}
