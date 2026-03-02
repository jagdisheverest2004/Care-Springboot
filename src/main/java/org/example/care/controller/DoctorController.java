package org.example.care.controller;

import java.util.Map;

import org.example.care.dto.appointment.CreateAppointmentRequest;
import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.auth.PatientRegistrationRequest;
import org.example.care.dto.doctor.GetDoctorProfile;
import org.example.care.dto.doctor.UpdateDoctorProfile;
import org.example.care.dto.drug.SafetyCheckRequest;
import org.example.care.dto.medicalrecord.MedicalRecordResponse;
import org.example.care.dto.patient.PatientRetreival;
import org.example.care.dto.patient.PatientsRetreival;
import org.example.care.dto.patient.UpdateConsultationRequest;
import org.example.care.model.*;
import org.example.care.model.enumeration.AppointmentStatus;
import org.example.care.model.enumeration.RiskLevel;
import org.example.care.security.CustomUserDetails;
import org.example.care.service.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DoctorController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DoctorService doctorService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/patients/consultation/{consultationId}/update")
    public ResponseEntity<String> updateConsultation(@PathVariable Long consultationId,
                                                     @RequestBody UpdateConsultationRequest updateConsultationRequest
    ) {
        Consultation consultation = consultationService.updateConsultation(consultationId, updateConsultationRequest);
        return ResponseEntity.ok("Consultation updated successfully for consultation id: " + consultation.getId());
    }

    @PreAuthorize("hasAnyRole('DOCTOR','PATIENT')")
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientRetreival> getPatient(@PathVariable Long patientId) {
        PatientRetreival patientRetreival = patientService.getPatientFullDetails(patientId);
        return ResponseEntity.ok(patientRetreival);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/patients")
    public ResponseEntity<PatientsRetreival> getPatients(@RequestParam(name = "patientName", required = false) String patientName,
                                                         @RequestParam(name = "riskLevel", required = false)RiskLevel riskLevel,
                                                         @AuthenticationPrincipal CustomUserDetails currentUser){
        PatientsRetreival patientsRetreival = patientService.searchPatientsByName(patientName,riskLevel,currentUser.getId());
        return ResponseEntity.ok(patientsRetreival);
    }


    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/patients/consultations/{consultationId}/records/xray", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecordResponse> uploadXrayForAnalysis(@PathVariable Long consultationId,
                                                                       @RequestParam("file") MultipartFile file
    ) {

        MedicalRecordResponse medicalRecordResponse = patientService.uploadXrayAndAnalyze(consultationId,file);
        return ResponseEntity.ok(medicalRecordResponse);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping(path = "/patients/consultations/{consultationId}/records/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MedicalRecordResponse> uploadReportForSummary(@PathVariable Long consultationId,
                                                                       @RequestParam("file") MultipartFile file
    ) {

        MedicalRecordResponse medicalRecordResponse = patientService.uploadReportAndSummarize(consultationId, file);
        return ResponseEntity.ok(medicalRecordResponse);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/patients/{patientId}/check-safety")
    public ResponseEntity<Map<String, Object>> checkDrugSafety(@PathVariable Long patientId, @Validated @RequestBody SafetyCheckRequest request) {

        Map<String,Object> response = patientService.checkDrugSafety(patientId,request);
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
                request
        );

        return ResponseEntity.ok(AuthResponse.builder()
                .message("Patient profile added to your account successfully")
                .build());
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/update-profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody UpdateDoctorProfile request) {

        AuthResponse response = doctorService.updateDoctorProfile(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/get-profile")
    public ResponseEntity<GetDoctorProfile> getDoctorProfile(){
        GetDoctorProfile response = doctorService.getDoctorProfile(currentUser().getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PatchMapping("/patients/appointments/{appointmentId}/scheduled")
    public ResponseEntity<String> updatePatientAppointmentStatus(@RequestParam(name = "status", required = false) AppointmentStatus appointmentStatus,
                                                                 @PathVariable Long appointmentId){
        String response = doctorService.updatePatientAppointmentStatus(appointmentStatus, appointmentId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/patients/appointments")
    public ResponseEntity<?> getDoctorAppointments(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                   @RequestParam(name = "status", required = false) AppointmentStatus appointmentStatus){
        return ResponseEntity.ok(doctorService.getDoctorAppointments(appointmentStatus, currentUser.getId()));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/patients/{patientId}/appointments")
    public ResponseEntity<String> schedulePatientAppointment(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                             @PathVariable Long patientId,
                                                             @RequestBody CreateAppointmentRequest request){
        String response = doctorService.schedulePatientAppointment(currentUser.getId(), patientId,request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/patients/consultations")
    public ResponseEntity<?> getDoctorConsultations(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(doctorService.getDoctorConsultations(currentUser.getId()));
    }

    protected CustomUserDetails currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
