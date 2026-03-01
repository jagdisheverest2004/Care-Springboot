package org.example.care.service;

import org.example.care.dto.drug.AddPatientDrugRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class PatientDrugService {

    @Autowired
    private DrugService drugService;

    @Autowired
    private PatientDrugRepository patientDrugRepository;


    @Transactional
    public void createPatientDrug(CreatePatientDoctorRequest visit, Patient existingPatient, Doctor consultingDoctor, PatientDoctor patientDoctor) {
        // 1. Guard clause: If no drugs are provided, do nothing.
        if (visit.getNewDrugs() == null || visit.getNewDrugs().isEmpty()) {
            return;
        }

        // 2. Initialize prescriptions list if it's null
        if (patientDoctor.getPrescriptions() == null) {
            patientDoctor.setPrescriptions(new ArrayList<>());
        }

        // 3. Create an O(1) lookup Map for existing drugs (Key: Drug ID, Value: PatientDrug Entity)
        Map<Long, PatientDrug> existingDrugMap = patientDoctor.getPrescriptions().stream()
                .collect(Collectors.toMap(pd -> pd.getDrug().getId(), pd -> pd));

        List<PatientDrug> drugsToUpdate = new ArrayList<>();
        List<AddPatientDrugRequest> drugsToCreate = new ArrayList<>();

        // 4. Sort incoming requests into "Updates" and "Creates"
        for (AddPatientDrugRequest requestDrug : visit.getNewDrugs()) {
            if (existingDrugMap.containsKey(requestDrug.getDrugId())) {
                // It exists! Update the entity fields.
                PatientDrug existingDrug = existingDrugMap.get(requestDrug.getDrugId());
                existingDrug.setDosage(requestDrug.getDosage());
                existingDrug.setInstructions(requestDrug.getInstructions());
                existingDrug.setStartDate(requestDrug.getStartDate());
                existingDrug.setEndDate(requestDrug.getEndDate());
                // Wrap in new ArrayList to avoid Hibernate immutable collection errors
                existingDrug.setDrugTimes(new ArrayList<>(requestDrug.getDrugTimes()));

                drugsToUpdate.add(existingDrug);
            } else {
                // It doesn't exist! Mark for creation.
                drugsToCreate.add(requestDrug);
            }
        }

        // 5. Save updates in a single batch
        if (!drugsToUpdate.isEmpty()) {
            patientDrugRepository.saveAll(drugsToUpdate);
        }

        // 6. Save new creations using the helper
        if (!drugsToCreate.isEmpty()) {
            helperCreatePatientDrug(existingPatient, consultingDoctor, patientDoctor, drugsToCreate);
        }
    }

    public void helperCreatePatientDrug(Patient existingPatient, Doctor consultingDoctor, PatientDoctor patientDoctor, List<AddPatientDrugRequest> newDrugs) {
        if (newDrugs == null || newDrugs.isEmpty()) return;

        // 1. Map DTOs to Entities cleanly without database side-effects in the stream
        List<PatientDrug> newPatientDrugs = newDrugs.stream().map(drugReq -> {
            PatientDrug patientDrug = new PatientDrug();
            patientDrug.setPatient(existingPatient);
            patientDrug.setDrug(drugService.getDrugById(drugReq.getDrugId()));
            patientDrug.setDosage(drugReq.getDosage());
            patientDrug.setInstructions(drugReq.getInstructions());
            patientDrug.setPrescribedBy(consultingDoctor);
            patientDrug.setStartDate(drugReq.getStartDate());
            patientDrug.setEndDate(drugReq.getEndDate());
            patientDrug.setDrugTimes(new ArrayList<>(drugReq.getDrugTimes())); // Avoid immutable lists
            patientDrug.setVisit(patientDoctor);
            return patientDrug;
        }).collect(Collectors.toList());

        // 2. Batch save to the database (Much faster than saving inside a loop)
        patientDrugRepository.saveAll(newPatientDrugs);

        // 3. Safely link to parent entities
        if (patientDoctor.getPrescriptions() == null) {
            patientDoctor.setPrescriptions(new ArrayList<>());
        }
        patientDoctor.getPrescriptions().addAll(newPatientDrugs);

        if (existingPatient.getPrescriptions() == null) {
            existingPatient.setPrescriptions(new ArrayList<>());
        }
        existingPatient.getPrescriptions().addAll(newPatientDrugs);
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
