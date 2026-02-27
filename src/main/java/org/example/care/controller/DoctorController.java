package org.example.care.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@SuppressWarnings("null")
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
    @PatchMapping("/doctor/patients/{patientId}")
    public ResponseEntity<String> updatePatient(@PathVariable Long patientId, @RequestBody UpdatePatient updatePatient) {
        Patient updatedPatient = patientService.updatePatient(patientId, updatePatient);
        return ResponseEntity.ok("Patient " + updatedPatient.getUser().getUsername() + " updated successfully");
    }

    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    @GetMapping("/doctor/patients/{patientId}")
    public ResponseEntity<PatientRetreival> getPatient(@PathVariable Long patientId) {
        PatientRetreival patientRetreival = patientService.getPatientFullDetails(patientId);
        return ResponseEntity.ok(patientRetreival);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/patients")
    public ResponseEntity<PatientsRetreival> getPatients(@RequestParam(name = "q", required = false) String query) {
        PatientsRetreival patientsRetreival = patientService.searchPatientsByName(query);
        return ResponseEntity.ok(patientsRetreival);
    }


    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/doctor/patients/{patientId}/records/xray", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadXrayForAnalysis(@PathVariable Long patientId,
                                                                      @RequestParam("file") MultipartFile file) {
        Patient patient = patientService.getPatientById(patientId);
        CustomUserDetails user = currentUser();
        User doctor = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Map<String, Object> aiSummary = aiOrchestrationService.analyzeXray(file);

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor.getDoctor())
                .type(MedicalRecordType.IMAGE)
                .fileName(file.getOriginalFilename())
                .summary(aiSummary.toString())
                .build();
        fileService.storeFile(record, file);
        MedicalRecord saved = medicalRecordRepository.save(record);

        return ResponseEntity.ok(Map.of(
                "record", MedicalRecordResponse.from(saved),
                "aiSummary", aiSummary));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/doctor/patients/{patientId}/records/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadReportForSummary(@PathVariable Long patientId,
                                                                       @RequestParam("file") MultipartFile file) {
        Patient patient = patientService.getPatientById(patientId);
        CustomUserDetails user = currentUser();
        User doctor = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));


        String narrative = aiOrchestrationService.summarizeReports(file);

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor.getDoctor())
                .type(MedicalRecordType.REPORT)
                .fileName(file.getOriginalFilename())
                .summary(narrative)
                .build();
        fileService.storeFile(record, file);
        MedicalRecord saved = medicalRecordRepository.save(record);

        return ResponseEntity.ok(Map.of(
                "record", MedicalRecordResponse.from(saved),
                "narrative", narrative));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/check-safety")
    public ResponseEntity<Map<String, Object>> checkDrugSafety(@Validated @RequestBody SafetyCheckRequest request) {
        return ResponseEntity.ok(aiOrchestrationService.checkSafety(request));
    }


    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT')")
    @GetMapping("/patients/{patientId}/records/{recordId}/file")
    public ResponseEntity<byte[]> downloadRecordFile(@PathVariable Long patientId, @PathVariable Long recordId) {
        validatePatientAccess(patientId);

        MedicalRecord record = medicalRecordRepository.findByIdAndPatientId(recordId, patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));
        byte[] file = fileService.readFile(record);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=record-" + recordId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
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

    private void validatePatientAccess(Long patientId) {
        CustomUserDetails user = currentUser();
        if (user.getRole() == Role.PATIENT && !patientId.equals(user.getId())) {
            throw new AccessDeniedException("Patients can only view their own records");
        }
    }

    private CustomUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
