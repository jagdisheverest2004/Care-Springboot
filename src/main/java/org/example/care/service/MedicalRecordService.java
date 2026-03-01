package org.example.care.service;

import org.example.care.dto.medicalrecord.MedicalRecordResponse;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.model.enumeration.MedicalRecordType;
import org.example.care.repository.DoctorRepository;
import org.example.care.repository.MedicalRecordRepository;
import org.example.care.repository.ConsultationRepository;
import org.example.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@SuppressWarnings("null")
public class MedicalRecordService {


    @Autowired
    private FileService fileService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private DoctorRepository doctorRepository;


    @Transactional
    public MedicalRecordResponse uploadMedicalRecord(Consultation consultation,MedicalRecordType medicalRecordType, MultipartFile file, String aiSummary) {

        MedicalRecord record = MedicalRecord.builder()
                .patient(consultation.getPatient())
                .doctor(consultation.getDoctor())
                .type(medicalRecordType)
                .fileName(file.getOriginalFilename())
                .summary(aiSummary)
                .consultation(consultation)
                .build();

        fileService.storeFile(record, file);
        MedicalRecord saved = medicalRecordRepository.save(record);
        consultation.getVisitRecords().add(saved);
        consultationRepository.save(consultation);
        consultation.getPatient().getMedicalRecords().add(saved);
        patientRepository.save(consultation.getPatient());
        consultation.getDoctor().getTreatedRecords().add(saved);
        doctorRepository.save(consultation.getDoctor());
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
