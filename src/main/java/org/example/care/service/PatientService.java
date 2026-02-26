package org.example.care.service;

import java.util.List;

import org.example.care.dto.CreatePatient;
import org.example.care.dto.MedicalRecordRetreival;
import org.example.care.dto.PatientRetreival;
import org.example.care.dto.UpdatePatient;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Patient;
import org.example.care.repository.MedicalRecordRepository;
import org.example.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    private MedicalRecordService medicalRecordService;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient save(CreatePatient createPatient) {
        Patient patient = Patient.builder()
                .name(createPatient.getName())
                .age(createPatient.getAge())
                .gender(createPatient.getGender())
                .bloodGroup(createPatient.getBloodGroup())
                .chronicConditions(createPatient.getChronicConditions())
                .currentMeds(createPatient.getNewMeds())
                .build();
        return patientRepository.save(patient);
    }

    public Patient getPatientByName(String name) {
        return patientRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with name: " + name));
    }

    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }

    public List<Patient> searchPatients(String query) {
        if (query == null || query.isBlank()) {
            return patientRepository.findAll();
        }
        return patientRepository.findByNameContainingIgnoreCase(query);
    }

    public Patient updatePatient(Long patientId, UpdatePatient updatePatient) {
        Patient existingPatient = getPatientById(patientId);
        if (updatePatient.getName() != null) {
            existingPatient.setName(updatePatient.getName());
        }
        if (updatePatient.getAge() != null) {
            existingPatient.setAge(updatePatient.getAge());
        }
        if (updatePatient.getGender() != null) {
            existingPatient.setGender(updatePatient.getGender());
        }
        if (updatePatient.getBloodGroup() != null) {
            existingPatient.setBloodGroup(updatePatient.getBloodGroup());
        }
        if (updatePatient.getChronicConditions() != null) {
            existingPatient.setChronicConditions(updatePatient.getChronicConditions());
        }

        if(updatePatient.getNewMeds() != null) {
            List<String> currentMeds = existingPatient.getCurrentMeds();
            if (currentMeds != null) {
                currentMeds.addAll(updatePatient.getNewMeds());
                existingPatient.setCurrentMeds(currentMeds);
            } else {
                existingPatient.setCurrentMeds(updatePatient.getNewMeds());
            }
        }

        return patientRepository.save(existingPatient);
    }

    public PatientRetreival getPatientFullDetails(Long patientId) {
        Patient patient = getPatientById(patientId);
        List<MedicalRecordRetreival> medicalRecords = medicalRecordService.findRecordsByPatientId(patientId);
        PatientRetreival patientRetreival = new PatientRetreival();
        patientRetreival.setId(patient.getId());
        patientRetreival.setName(patient.getName());
        patientRetreival.setAge(patient.getAge());
        patientRetreival.setGender(patient.getGender());
        patientRetreival.setBloodGroup(patient.getBloodGroup());
        patientRetreival.setChronicConditions(patient.getChronicConditions());
        patientRetreival.setCurrentMeds(patient.getCurrentMeds());
        patientRetreival.setMedicalRecords(medicalRecords);
        return patientRetreival;
    }
}
