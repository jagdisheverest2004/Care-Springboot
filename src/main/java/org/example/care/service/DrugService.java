package org.example.care.service;

import org.example.care.dto.AddDrugRequest;
import org.example.care.model.Drug;
import org.example.care.repository.DrugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class DrugService {

    @Autowired
    private DrugRepository drugRepository;

    public String createDrug(AddDrugRequest request) {
        Drug drug = new Drug();
        drug.setDrugName(request.getDrugName());
        drug.setGeneralDescription(request.getGeneralDescription());
        drugRepository.save(drug);
        return "Drug"+" "+request.getDrugName()+" added successfully.";
    }

    public Drug getDrugById(Long drugId) {
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("Drug not found with id: " + drugId));
    }
}
