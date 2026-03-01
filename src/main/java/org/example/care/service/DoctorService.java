package org.example.care.service;

import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.doctor.UpdateDoctorProfile;
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

    public AuthResponse updateDoctorProfile(Long id, UpdateDoctorProfile request) {
        Doctor doctor = getDoctorByUserId(id);

        if (request.getSpecialization() != null) {
            doctor.setSpecialization(request.getSpecialization());
        }

        if (request.getName() != null) {
            doctor.setName(request.getName());
        }

        if (request.getHospitalName() != null) {
            doctor.setHospitalName(request.getHospitalName());
        }

        if (request.getContactInfo() != null) {
            doctor.setContactInfo(request.getContactInfo());
        }

        doctorRepository.save(doctor);

        return AuthResponse.builder()
                .message("Doctor profile updated successfully")
                .role(doctor.getUser().getRole())
                .username(doctor.getUser().getUsername())
                .build();
    }
}
