package org.example.care.service;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.care.dto.auth.AuthResponse;
import org.example.care.dto.drug.SafetyCheckRequest;
import org.example.care.dto.medicalrecord.MedicalRecordResponse;
import org.example.care.dto.patient.*;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.*;
import org.example.care.model.enumeration.MedicalRecordType;
import org.example.care.model.enumeration.RiskLevel;
import org.example.care.repository.PatientDoctorRepository;
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
    private PatientDoctorRepository patientDoctorRepository;

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
    public Patient updatePatient(Long patientId,Long patientDoctorId, UpdatePatientConditionsRequest updatePatient, CustomUserDetails customUserDetails) {
        Patient existingPatient = getPatientById(patientId);
        Doctor doctor = doctorService.getDoctorByUserId(customUserDetails.getId());

        if (updatePatient.getChronicConditions() != null) {
            existingPatient.setChronicConditions(updatePatient.getChronicConditions());
        }

        CreatePatientDoctorRequest doctorVisit = updatePatient.getDoctorVisit();

        if(doctorVisit != null) {
            patientDoctorService.createVisitation(existingPatient, patientDoctorId, doctor, doctorVisit);
        }

        return patientRepository.save(existingPatient);
    }


    public PatientRetreival getPatientFullDetails(Long patientId) {
        Patient patient = getPatientById(patientId);
        PatientRetreival patientRetreival = new PatientRetreival();
        patientRetreival.setId(patient.getId());
        patientRetreival.setName(patient.getUser().getPatient().getName());
        patientRetreival.setAge(patient.getAge());
        patientRetreival.setGender(patient.getGender());
        patientRetreival.setDateOfBirth(patient.getDateOfBirth());
        patientRetreival.setAddress(patient.getAddress());
        patientRetreival.setContactNumber(patient.getContactNumber());
        patientRetreival.setBloodGroup(patient.getBloodGroup());
        patientRetreival.setChronicConditions(patient.getChronicConditions());

        List<PatientDoctorRetreival> doctorVisits = patientDoctorService.getPatientDoctorDetails(patient.getDoctorVisits());
        patientRetreival.setDoctorVisits(doctorVisits);

        return patientRetreival;
    }

    public PatientsRetreival searchPatientsByName(String patientName, RiskLevel riskLevel, Long doctorUserId) {
        List<Patient> patients;
        List<PatientDoctor> visitedPatients = patientDoctorRepository.findVisitedPatientsByDoctorUserId(doctorUserId);
        patients = visitedPatients.stream().map(PatientDoctor::getPatient).toList();
        if (patientName != null && !patientName.isEmpty()) {
            patients = patients.stream()
                    .filter(patient -> patient.getUser().getPatient().getName().toLowerCase().contains(patientName.toLowerCase()))
                    .collect(Collectors.toList());

            if(patients.isEmpty()) {
                throw new ResourceNotFoundException("No patients found with name containing: " + patientName);
            }
            if(riskLevel != null) {
                patients = getPatientsByRiskLevel(patients,riskLevel);

                if(patients.isEmpty()) {
                    throw new ResourceNotFoundException("No patients found with name containing: " + patientName + " and risk level: " + riskLevel);
                }
            }
        }
        else if(riskLevel != null) {
            patients = getPatientsByRiskLevel(patients,riskLevel);

            if(patients.isEmpty()) {
                throw new ResourceNotFoundException("No patients found with risk level: " + riskLevel);
            }
        }


        List<PatientRetreival> patientRetreivals = patients.stream().map(patient -> this.getPatientFullDetails(patient.getId())).collect(Collectors.toList());

        PatientsRetreival patientsRetreival = new PatientsRetreival();
        patientsRetreival.setPatients(patientRetreivals);
        return patientsRetreival;
    }

    public List<Patient> getPatientsByRiskLevel(List<Patient> patients,RiskLevel riskLevel) {
        return patients.stream()
                .filter(patient -> patient.getDoctorVisits().stream()
                        .allMatch(visit -> visit.getRiskLevel() == riskLevel))
                .collect(Collectors.toList());
    }

    public MedicalRecordResponse uploadXrayAndAnalyze(Long patientId, Long patientDoctorId, MultipartFile file, CustomUserDetails customUserDetails) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        String aiSummary = aiOrchestrationService.analyzeXray(file);

        return medicalRecordService.uploadMedicalRecord(patient,doctor.getDoctor(), MedicalRecordType.IMAGE, patientDoctorId, file, aiSummary);
    }

    public MedicalRecordResponse uploadReportAndSummarize(Long patientId, Long patientDoctorId, MultipartFile file, CustomUserDetails customUserDetails) {
        Patient patient = this.getPatientById(patientId);
        User doctor = userRepository.findById(customUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        String narrative = aiOrchestrationService.summarizeReports(file);
        return medicalRecordService.uploadMedicalRecord(patient, doctor.getDoctor(),MedicalRecordType.REPORT, patientDoctorId, file, narrative);
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

    public AuthResponse updatePatientProfile(Long id, UpdatePatientProfile request) {
        Patient patient = this.getPatientById(id);

        if (request.getName() != null) {
            patient.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            patient.setContactNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            patient.setAddress(request.getAddress());
        }

        patientRepository.save(patient);

        return AuthResponse.builder().message("Patient profile updated successfully")
                .role(patient.getUser().getRole())
                .username(patient.getUser().getUsername())
                .build();
    }
}
