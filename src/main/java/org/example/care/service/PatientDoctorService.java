package org.example.care.service;

import org.example.care.model.PatientDoctor;
import org.example.care.repository.PatientDoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientDoctorService {

    @Autowired
    private PatientDoctorRepository patientDoctorRepository;

    public PatientDoctor getPatientDoctorById(Long patientDoctorId) {
        return patientDoctorRepository.findById(patientDoctorId).orElseThrow(() -> new IllegalArgumentException("Drug not found with id: " + patientDoctorId));
    }
}
