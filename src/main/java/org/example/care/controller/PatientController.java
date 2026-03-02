package org.example.care.controller;

import org.example.care.dto.appointment.CreateAppointmentRequest;
import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.auth.DoctorRegistrationRequest;
import org.example.care.dto.patient.UpdatePatientProfile;
import org.example.care.security.CustomUserDetails;
import org.example.care.service.AuthService;
import org.example.care.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@SuppressWarnings("null")
public class PatientController {


    @Autowired
    private AuthService authService;

    @Autowired
    private PatientService patientService;

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

    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/update-profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody UpdatePatientProfile request) {

        AuthResponse response = patientService.updatePatientProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/create-appointment/doctor/{doctorId}")
    public ResponseEntity<String> createAppointment(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long doctorId,
            CreateAppointmentRequest request){
        String response = patientService.createAppointment(currentUser.getId(),doctorId, request);
        return ResponseEntity.ok(response);
    }
}
