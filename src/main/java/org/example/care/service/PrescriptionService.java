package org.example.care.service;

import org.example.care.dto.drug.PatientDrugRetreival;
import org.example.care.dto.patient.CreateConsultationRequest;
import org.example.care.model.Consultation;
import org.example.care.model.Prescription;
import org.example.care.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class PrescriptionService {

    @Autowired
    private DrugService drugService;

    @Autowired
    private PrescriptionRepository prescriptionRepository;


    @Transactional
    public void createPatientDrug(CreateConsultationRequest visit, Consultation consultation) {

        if (visit.getNewDrugs() == null || visit.getNewDrugs().isEmpty()) {
            return;
        }

        if (consultation.getPrescriptions() == null) {
            consultation.setPrescriptions(new ArrayList<>());
        }

        List<Prescription> newPrescriptions = visit.getNewDrugs().stream().map(drugReq -> {
            Prescription prescription = new Prescription();
            prescription.setPatient(consultation.getPatient());
            prescription.setDrug(drugService.getDrugById(drugReq.getDrugId()));
            prescription.setDosage(drugReq.getDosage());
            prescription.setInstructions(drugReq.getInstructions());
            prescription.setPrescribedBy(consultation.getDoctor());
            prescription.setStartDate(drugReq.getStartDate());
            prescription.setEndDate(drugReq.getEndDate());
            prescription.setDrugTimes(new ArrayList<>(drugReq.getDrugTimes())); // Avoid immutable lists
            prescription.setVisit(consultation);
            return prescription;
        }).collect(Collectors.toList());

        prescriptionRepository.saveAll(newPrescriptions);

        consultation.getPrescriptions().addAll(newPrescriptions);

        if (consultation.getPatient().getPrescriptions() == null) {
            consultation.getPatient().setPrescriptions(new ArrayList<>());
        }

        if(consultation.getDoctor().getWrittenPrescriptions() == null) {
            consultation.getDoctor().setWrittenPrescriptions(new ArrayList<>());
        }

        consultation.getPatient().getPrescriptions().addAll(newPrescriptions);
        consultation.getDoctor().getWrittenPrescriptions().addAll(newPrescriptions);
    }

    public List<PatientDrugRetreival> getPatientDrugDetails(List<Prescription> prescriptions) {
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
