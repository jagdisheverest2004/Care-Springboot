package org.example.care.service;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;

import org.example.care.dto.drug.SafetyCheckRequest;
import org.example.care.dto.medicalrecord.MedicalRecordResponse;
import org.example.care.dto.patient.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.model.enumeration.MedicalRecordType;
import org.example.care.repository.PatientRepository;
import org.example.care.repository.UserRepository;
import org.example.care.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@SuppressWarnings("null")
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AiOrchestrationService aiOrchestrationService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientDoctorService patientDoctorService;


    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }


    @Transactional
    public Patient updatePatient(Long patientId, UpdatePatientConditionsRequest updatePatient, CustomUserDetails customUserDetails) {
        Patient existingPatient = getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorByUserId(customUserDetails.getId());

        if (updatePatient.getChronicConditions() != null) {
            existingPatient.setChronicConditions(updatePatient.getChronicConditions());
        }

        CreatePatientDoctorRequest doctorVisit = updatePatient.getDoctorVisit();

        if(doctorVisit != null) {
            patientDoctorService.createVisitation(existingPatient, doctor, doctorVisit);
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

        List<PatientDoctorRetreival> doctorVisits = patientDoctorService.getPatientDoctorDetails(patient.getDoctorVisits());
        patientRetreival.setDoctorVisits(doctorVisits);

        return patientRetreival;
    }

    public PatientsRetreival searchPatientsByName(String query) {
        List<Patient> patients;
        if (query == null || query.isBlank()) {
            patients = patientRepository.findAll();
        } else {
            patients = patientRepository.findByNameContainingIgnoreCase(query);
        }

        List<PatientRetreival> patientRetreivals = patients.stream().map(patient -> this.getPatientFullDetails(patient.getId())).toList();

        PatientsRetreival patientsRetreival = new PatientsRetreival();
        patientsRetreival.setPatients(patientRetreivals);
        return patientsRetreival;
    }

    public MedicalRecordResponse uploadXrayAndAnalyze(Long patientId, Long patientDoctorId, MultipartFile file, CustomUserDetails customUserDetails) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Map<String, Object> aiSummary = aiOrchestrationService.analyzeXray(file);

        return medicalRecordService.uploadMedicalRecord(patient,doctor.getDoctor(), MedicalRecordType.IMAGE, patientDoctorId, file, aiSummary);
    }

    public MedicalRecordResponse uploadReportAndSummarize(Long patientId, Long patientDoctorId, MultipartFile file, CustomUserDetails customUserDetails) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        String narrative = aiOrchestrationService.summarizeReports(file);
        return medicalRecordService.uploadMedicalRecord(patient, doctor.getDoctor(),MedicalRecordType.REPORT, patientDoctorId, file, Map.of("summary", narrative));
    }

    public Map<String, Object> checkDrugSafety(SafetyCheckRequest request) {
        Patient patient = this.getPatientById(request.getPatientId());
        List<String> currentDrugNames = patient.getPrescriptions().stream()
                .filter(pd -> pd.getEndDate().isAfter(ChronoLocalDate.from(LocalDate.now())))
                .map(pd -> pd.getDrug().getDrugName())
                .toList();

        return aiOrchestrationService.checkSafety(patient.getId(),currentDrugNames,request.getNewDrugs());
    }

    public MedicalRecord getMedicalRecordForPatient(Long patientId, Long recordId) {
        this.getPatientById(patientId);
        MedicalRecord record = medicalRecordService.getMedicalRecordById(recordId);
        if (!record.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Medical record not found for patient with id: " + patientId);
        }
        return record;
    }
}
