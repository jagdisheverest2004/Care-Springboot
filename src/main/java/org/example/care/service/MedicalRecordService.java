package org.example.care.service;

import org.example.care.dto.MedicalRecordRetreival;
import org.example.care.model.Patient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class MedicalRecordService {


    public List<MedicalRecordRetreival> getOverviewOfRecords(Patient patient) {
        List<MedicalRecordRetreival> overview = patient.getMedicalRecords().stream()
                .map(record -> new MedicalRecordRetreival(
                        record.getId(),
                        record.getFileName(),
                        record.getType().name(),
                        record.getSummary(),
                        record.getDoctor().getId(),
                        record.getDoctor().getUser().getUsername()
                ))
                .toList();
        return overview;
    }
}
