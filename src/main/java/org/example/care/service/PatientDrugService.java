package org.example.care.service;

import org.example.care.dto.drug.PatientDrugRetreival;
import org.example.care.dto.patient.CreatePatientDoctorRequest;
import org.example.care.model.Doctor;
import org.example.care.model.Patient;
import org.example.care.model.PatientDoctor;
import org.example.care.model.PatientDrug;
import org.example.care.repository.PatientDrugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@SuppressWarnings("null")
public class PatientDrugService {

    @Autowired
    private DrugService drugService;

    @Autowired
    private PatientDrugRepository patientDrugRepository;

    @Transactional
    public void createPatientDrug(CreatePatientDoctorRequest visit, Patient existingPatient, Doctor consultingDoctor, PatientDoctor patientDoctor) {
        List<PatientDrug> patientDrugs = visit.getNewDrugs().stream().map(drug -> {
            PatientDrug patientDrug = new PatientDrug();
            patientDrug.setPatient(existingPatient);
            patientDrug.setDrug(drugService.getDrugById(drug.getDrugId()));
            patientDrug.setDosage(drug.getDosage());
            patientDrug.setInstructions(drug.getInstructions());
            patientDrug.setPrescribedBy(consultingDoctor);
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

    public List<PatientDrugRetreival> getPatientDrugDetails(List<PatientDrug> prescriptions) {
        return prescriptions.stream().map(patientDrug -> {
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
    }
}
