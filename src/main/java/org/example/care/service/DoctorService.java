package org.example.care.service;

import org.example.care.model.Doctor;
import org.example.care.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor getDoctorByUserId(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with user id: " + id));
    }
}
