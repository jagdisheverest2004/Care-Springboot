package org.example.care.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;

import org.example.care.dto.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.repository.PatientDoctorRepository;
import org.example.care.repository.PatientDrugRepository;
import org.example.care.repository.PatientRepository;
import org.example.care.repository.UserRepository;
import org.example.care.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@SuppressWarnings("null")
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientDrugRepository patientDrugRepository;

    @Autowired
    private PatientDoctorRepository patientDoctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AiOrchestrationService aiOrchestrationService;

    @Autowired
    private CustomUserDetails currentUser;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DrugService drugService;


    public Patient getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    }


    public Patient updatePatient(Long patientId, UpdatePatientConditionsRequest updatePatient, CustomUserDetails customUserDetails) {
        Patient existingPatient = getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorByUserId(customUserDetails.getId());

        if (updatePatient.getChronicConditions() != null) {
            existingPatient.setChronicConditions(updatePatient.getChronicConditions());
        }

        CreatePatientDoctorRequest doctorVisit = updatePatient.getDoctorVisit();

        if(doctorVisit != null) {
            PatientDoctor patientDoctor = new PatientDoctor();
            patientDoctor.setPatient(existingPatient);
            patientDoctor.setDoctor(doctor);
            patientDoctor.setPurpose(doctorVisit.getPurpose());
            patientDoctor.setNotes(doctorVisit.getNotes());
            patientDoctor.setVisitedAt(LocalDateTime.now());
            patientDoctorRepository.save(patientDoctor);
            if(doctorVisit.getNewDrugs() != null) {
                List<PatientDrug> patientDrugs = doctorVisit.getNewDrugs().stream().map(drug -> {
                    PatientDrug patientDrug = new PatientDrug();
                    patientDrug.setPatient(existingPatient);
                    patientDrug.setDrug(drugService.getDrugById(drug.getDrugId()));
                    patientDrug.setDosage(drug.getDosage());
                    patientDrug.setInstructions(drug.getInstructions());
                    patientDrug.setPrescribedBy(doctor);
                    patientDrug.setStartDate(drug.getStartDate());
                    patientDrug.setEndDate(drug.getEndDate());
                    patientDrug.setDrugTimes(drug.getDrugTimes());
                    patientDrug.setVisit(patientDoctor);
                    patientDrugRepository.save(patientDrug);
                    return patientDrug;
                }).toList();
                patientDoctor.setPrescriptions(patientDrugs);
                existingPatient.setPrescriptions(patientDrugs);
            }
            patientDoctorRepository.save(patientDoctor);
            existingPatient.getDoctorVisits().add(patientDoctor);
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
        List<PatientDoctorRetreival> doctorVisits = patient.getDoctorVisits().stream().map(visit -> {
            Doctor treatedDoctor = visit.getDoctor();
            PatientDoctorRetreival visitRetreival = new PatientDoctorRetreival();
            visitRetreival.setId(visit.getId());
            visitRetreival.setPrescribedDoctorId(treatedDoctor.getId());
            visitRetreival.setPrescribedDoctorName(treatedDoctor.getName());
            visitRetreival.setPurpose(visit.getPurpose());
            visitRetreival.setNotes(visit.getNotes());
            visitRetreival.setVisitedAt(visit.getVisitedAt());

            List<PatientDrugRetreival> drugRetreivals = visit.getPrescriptions().stream().map(patientDrug -> {
                PatientDrugRetreival drugRetreival = new PatientDrugRetreival();
                drugRetreival.setId(patientDrug.getId());
                drugRetreival.setDrugId(patientDrug.getDrug().getId());
                drugRetreival.setDrugName(patientDrug.getDrug().getDrugName());
                drugRetreival.setDosage(patientDrug.getDosage());
                drugRetreival.setInstructions(patientDrug.getInstructions());
                drugRetreival.setStartDate(patientDrug.getStartDate());
                drugRetreival.setEndDate(patientDrug.getEndDate());
                drugRetreival.setDrugTimes(patientDrug.getDrugTimes());
                return drugRetreival;
            }).toList();

            List<MedicalRecordRetreival> recordRetreivals = visit.getVisitRecords().stream().map(record -> {
                MedicalRecordRetreival recordRetreival = new MedicalRecordRetreival();
                recordRetreival.setRecordId(record.getId());
                recordRetreival.setFileName(record.getFileName());
                recordRetreival.setFileType(record.getType().name());
                recordRetreival.setFileSummary(record.getSummary());
                recordRetreival.setUploadedAt(record.getCreatedAt());
                return recordRetreival;
            }).toList();

            visitRetreival.setDrugsPrescribed(drugRetreivals);
            visitRetreival.setMedicalRecords(recordRetreivals);

            return visitRetreival;
        }).toList();

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

    public MedicalRecordResponse uploadXrayAndAnalyze(Long patientId, Long patientDoctorId, MultipartFile file) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Map<String, Object> aiSummary = aiOrchestrationService.analyzeXray(file);

        return medicalRecordService.uploadMedicalRecord(patient,doctor.getDoctor(),MedicalRecordType.IMAGE, patientDoctorId, file, aiSummary);
    }

    public MedicalRecordResponse uploadReportAndSummarize(Long patientId, Long patientDoctorId, MultipartFile file) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(currentUser.getId())
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
