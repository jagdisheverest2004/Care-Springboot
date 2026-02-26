package org.example.care.service;

import java.util.List;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Patient;
import org.example.care.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
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
}
