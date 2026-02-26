package org.example.care.service;

import org.example.care.dto.MedicalRecordRetreival;
import org.example.care.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;


    public List<MedicalRecordRetreival> findRecordsByPatientId(Long patientId) {
        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(record -> {
                    MedicalRecordRetreival retreival = new MedicalRecordRetreival();
                    retreival.setRecordId(record.getId());
                    retreival.setFileName(record.getFileName());
                    retreival.setFileType(record.getType().name());
                    retreival.setFileSummary(record.getSummary());
                    return retreival;
                })
                .toList();

    }
}
