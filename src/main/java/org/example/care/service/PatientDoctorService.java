package org.example.care.service;

import org.example.care.dto.drug.PatientDrugRetreival;
import org.example.care.dto.medicalrecord.MedicalRecordRetreival;
import org.example.care.dto.patient.CreatePatientDoctorRequest;
import org.example.care.dto.patient.PatientDoctorRetreival;
import org.example.care.exception.ResourceNotFoundException;
import org.example.care.model.Doctor;
import org.example.care.model.Patient;
import org.example.care.model.PatientDoctor;
import org.example.care.repository.PatientDoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatientDoctorService {

    @Autowired
    private PatientDoctorRepository patientDoctorRepository;

    @Autowired
    private PatientDrugService patientDrugService;

    @Autowired
    private MedicalRecordService medicalRecordService;


    @Transactional
    public void createVisitation(Patient existingPatient,Long patientDoctorId , Doctor consultingDoctor, CreatePatientDoctorRequest visit){

        PatientDoctor patientDoctor = patientDoctorId != null ?
                patientDoctorRepository.findById(patientDoctorId).orElseThrow(()-> new ResourceNotFoundException("Visit not found with id: " + patientDoctorId)) : new PatientDoctor();
        patientDoctor.setPatient(existingPatient);
        patientDoctor.setDoctor(consultingDoctor);
        patientDoctor.setPurpose(visit.getPurpose());
        patientDoctor.setNotes(visit.getNotes());
        patientDoctor.setRiskLevel(visit.getRiskLevel());
        patientDoctor.setVisitedAt(LocalDateTime.now());
        patientDoctorRepository.save(patientDoctor);
        if(visit.getNewDrugs() != null) {
            patientDrugService.createPatientDrug(visit,existingPatient,consultingDoctor,patientDoctor);
        }
        patientDoctorRepository.save(patientDoctor);
        existingPatient.getDoctorVisits().add(patientDoctor);

    }

    public List<PatientDoctorRetreival> getPatientDoctorDetails(List<PatientDoctor> doctorVisits) {

        return doctorVisits.stream().map(visit -> {
            Doctor treatedDoctor = visit.getDoctor();
            PatientDoctorRetreival visitRetreival = new PatientDoctorRetreival();
            visitRetreival.setId(visit.getId());
            visitRetreival.setPrescribedDoctorId(treatedDoctor.getId());
            visitRetreival.setPrescribedDoctorName(treatedDoctor.getName());
            visitRetreival.setPurpose(visit.getPurpose());
            visitRetreival.setNotes(visit.getNotes());
            visitRetreival.setVisitedAt(visit.getVisitedAt());
            visitRetreival.setRiskLevel(visit.getRiskLevel());

            List<PatientDrugRetreival> drugRetreivals = patientDrugService.getPatientDrugDetails(visit.getPrescriptions());

            List<MedicalRecordRetreival> recordRetreivals = medicalRecordService.getMedicalRecordDetails(visit.getVisitRecords());

            visitRetreival.setDrugsPrescribed(drugRetreivals);
            visitRetreival.setMedicalRecords(recordRetreivals);

            return visitRetreival;
        }).toList();
    }
}
