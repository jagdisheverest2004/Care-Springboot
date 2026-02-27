package org.example.care.service;

import java.util.List;

import org.example.care.dto.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Patient;
import org.example.care.repository.MedicalRecordRepository;
import org.example.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private MedicalRecordService medicalRecordService;



    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }


    public Patient updatePatient(Long patientId, UpdatePatient updatePatient) {
        Patient existingPatient = getPatientById(patientId);

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
        PatientRetreival patientRetreival = new PatientRetreival();
        patientRetreival.setId(patient.getId());
        patientRetreival.setName(patient.getUser().getUsername());
        patientRetreival.setAge(patient.getAge());
        patientRetreival.setGender(patient.getGender());
        patientRetreival.setBloodGroup(patient.getBloodGroup());
        patientRetreival.setChronicConditions(patient.getChronicConditions());
        patientRetreival.setCurrentMeds(patient.getCurrentMeds());

        List<MedicalRecordRetreival> records = medicalRecordService.getOverviewOfRecords(patient);
        patientRetreival.setMedicalRecords(records);

        return patientRetreival;
    }

    public PatientsRetreival searchPatientsByName(String query) {
        List<Patient> patients;
        if (query == null || query.isBlank()) {
            patients = patientRepository.findAll();
        } else {
            patients = patientRepository.findByNameContainingIgnoreCase(query);
        }

        List<PatientRetreival> patientRetreivals = patients.stream().map(patient -> {
            return this.getPatientFullDetails(patient.getId());
        }).toList();

        PatientsRetreival patientsRetreival = new PatientsRetreival();
        patientsRetreival.setPatients(patientRetreivals);
        return patientsRetreival;
    }
}
