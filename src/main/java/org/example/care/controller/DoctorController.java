package org.example.care.controller;

import java.util.Map;

import org.example.care.dto.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.repository.MedicalRecordRepository;
import org.example.care.repository.UserRepository;
import org.example.care.security.CustomUserDetails;
import org.example.care.service.AiOrchestrationService;
import org.example.care.service.AuthService;
import org.example.care.service.FileService;
import org.example.care.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/doctor")
@SuppressWarnings("null")
@Configuration
public class DoctorController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AiOrchestrationService aiOrchestrationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserRepository userRepository;


    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/patients/{patientId}")
    public ResponseEntity<String> updatePatient(@PathVariable Long patientId, @RequestBody UpdatePatientConditionsRequest updatePatient) {
        Patient updatedPatient = patientService.updatePatient(patientId, updatePatient,currentUser());
        return ResponseEntity.ok("Patient " + updatedPatient.getUser().getUsername() + " updated successfully");
    }

    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientRetreival> getPatient(@PathVariable Long patientId) {
        PatientRetreival patientRetreival = patientService.getPatientFullDetails(patientId);
        return ResponseEntity.ok(patientRetreival);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/patients")
    public ResponseEntity<PatientsRetreival> getPatients(@RequestParam(name = "q", required = false) String query) {
        PatientsRetreival patientsRetreival = patientService.searchPatientsByName(query);
        return ResponseEntity.ok(patientsRetreival);
    }


    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/patients/{patientId}/records/xray", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecordResponse> uploadXrayForAnalysis(@PathVariable Long patientId,
                                                                      @RequestParam("patientDoctorId") Long patientDoctorId,
                                                                      @RequestParam("file") MultipartFile file) {

        MedicalRecordResponse medicalRecordResponse = patientService.uploadXrayAndAnalyze(patientId, patientDoctorId, file);
        return ResponseEntity.ok(medicalRecordResponse);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/patients/{patientId}/records/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecordResponse> uploadReportForSummary(@PathVariable Long patientId,
                                                                       @RequestParam("patientDoctorId") Long patientDoctorId,
                                                                       @RequestParam("file") MultipartFile file) {

        MedicalRecordResponse medicalRecordResponse = patientService.uploadReportAndSummarize(patientId, patientDoctorId, file);
        return ResponseEntity.ok(medicalRecordResponse);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/check-safety")
    public ResponseEntity<Map<String, Object>> checkDrugSafety(@Validated @RequestBody SafetyCheckRequest request) {

        Map<String,Object> response = patientService.checkDrugSafety(request);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    @GetMapping("/patients/{patientId}/records/{recordId}/file")
    public ResponseEntity<byte[]> downloadRecordFile(@PathVariable Long patientId, @PathVariable Long recordId) {

        MedicalRecord record = patientService.getMedicalRecordForPatient(patientId, recordId);
        byte[] file = fileService.readFile(record);

        // Determine the filename with proper extension
        String fileName = (record.getFileName() != null) ? record.getFileName() : "record-" + recordId + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                // Setting a more specific type helps the browser recognize the file
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/extend/to-patient")
    public ResponseEntity<AuthResponse> extendToPatient(
            @Validated
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody PatientRegistrationRequest request) {

        authService.extendDoctorToPatient(
                currentUser.getId(),
                request.getAge(),
                request.getGender(),
                request.getBloodGroup()
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .message("Patient profile added to your account successfully")
                .build());
    }


    @Bean
    protected CustomUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
