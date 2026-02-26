package org.example.care.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.care.dto.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.MedicalRecord;
import org.example.care.model.MedicalRecordType;
import org.example.care.model.Patient;
import org.example.care.model.Role;
import org.example.care.repository.MedicalRecordRepository;
import org.example.care.security.CustomUserDetails;
import org.example.care.service.AiOrchestrationService;
import org.example.care.service.FileService;
import org.example.care.service.PatientService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@SuppressWarnings("null")
public class DoctorController {

    private final PatientService patientService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AiOrchestrationService aiOrchestrationService;
    private final FileService fileService;

    public DoctorController(PatientService patientService,
                            MedicalRecordRepository medicalRecordRepository,
                            AiOrchestrationService aiOrchestrationService,
                            FileService fileService) {
        this.patientService = patientService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.aiOrchestrationService = aiOrchestrationService;
        this.fileService = fileService;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/doctor/patients")
    public ResponseEntity<Patient> createPatient(@Validated @RequestBody CreatePatient createPatient) {
        return ResponseEntity.ok(patientService.save(createPatient));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/doctor/patients/{patientId}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long patientId, @RequestBody UpdatePatient updatePatient) {
        Patient updatedPatient = patientService.updatePatient(patientId, updatePatient);
        return ResponseEntity.ok(updatedPatient);
    }

    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    @GetMapping("/doctor/patients/{patientId}")
    public ResponseEntity<PatientRetreival> getPatient(@PathVariable Long patientId) {
        PatientRetreival patientRetreival = patientService.getPatientFullDetails(patientId);
        return ResponseEntity.ok(patientRetreival);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/patients")
    public ResponseEntity<List<Patient>> getPatients(@RequestParam(name = "q", required = false) String query) {
        return ResponseEntity.ok(patientService.searchPatients(query));
    }


    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/doctor/patients/{patientId}/records/xray", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadXrayForAnalysis(@PathVariable Long patientId,
                                                                      @RequestParam("file") MultipartFile file) {
        patientService.getPatientById(patientId);
        CustomUserDetails user = currentUser();

        Map<String, Object> aiSummary = aiOrchestrationService.analyzeXray(file);

        MedicalRecord record = MedicalRecord.builder()
                .patientId(patientId)
                .doctorId(user.getId())
                .type(MedicalRecordType.XRAY)
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
        patientService.getPatientById(patientId);
        CustomUserDetails user = currentUser();

        String narrative = aiOrchestrationService.summarizeReports(file);

        MedicalRecord record = MedicalRecord.builder()
                .patientId(patientId)
                .doctorId(user.getId())
                .type(MedicalRecordType.REPORT)
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
    @GetMapping("/patients/{patientId}/records")
    public ResponseEntity<List<MedicalRecordResponse>> getPatientRecords(@PathVariable Long patientId) {

        List<MedicalRecordResponse> records = medicalRecordRepository.findByPatientId(patientId).stream()
                .map(MedicalRecordResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(records);
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
