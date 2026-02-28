package org.example.care.controller;

import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.auth.DoctorRegistrationRequest;
import org.example.care.security.CustomUserDetails;
import org.example.care.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@SuppressWarnings("null")
public class PatientController {


    @Autowired
    private AuthService authService;

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/extend/to-doctor")
    public ResponseEntity<AuthResponse> extendToDoctor(
            @Validated
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody DoctorRegistrationRequest request) {

        authService.extendPatientToDoctor(
                currentUser.getId(),
                request.getSpecialization(),
                request.getLicenseNumber(),
                request.getHospitalName(),
                request.getContactInfo()
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .message("Doctor profile added to your account successfully")
                .build());
    }
}
