package org.example.care.service;

import org.example.care.dto.medicalrecord.MedicalRecordResponse;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class MedicalRecordService {

    @Autowired
    private PatientDoctorService patientDoctorService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;


    @Transactional
    public MedicalRecordResponse uploadMedicalRecord(Patient patient, Doctor doctor, MedicalRecordType medicalRecordType,Long patientDoctorId, MultipartFile file, Map<String, Object> aiSummary) {

        PatientDoctor patientDoctor = patientDoctorService.getPatientDoctorById(patientDoctorId);

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .type(medicalRecordType)
                .fileName(file.getOriginalFilename())
                .summary(aiSummary.toString())
                .patientDoctor(patientDoctor)
                .build();

        fileService.storeFile(record, file);
        MedicalRecord saved = medicalRecordRepository.save(record);
        return MedicalRecordResponse.from(saved);
    }

    public MedicalRecord getMedicalRecordById(Long recordId) {
        return medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
    }

    public List<MedicalRecordRetreival> getMedicalRecordDetails(List<MedicalRecord> visitRecords) {
        return visitRecords.stream().map(record -> {
            MedicalRecordRetreival recordRetreival = new MedicalRecordRetreival();
            recordRetreival.setRecordId(record.getId());
            recordRetreival.setFileName(record.getFileName());
            recordRetreival.setFileType(record.getType().name());
            recordRetreival.setFileSummary(record.getSummary());
            recordRetreival.setUploadedAt(record.getCreatedAt());
            return recordRetreival;
        }).toList();
    }
}
