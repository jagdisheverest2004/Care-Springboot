package org.example.care.controller;

import org.example.care.dto.drug.AddDrugRequest;
import org.example.care.service.DrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drugs")
@SuppressWarnings("null")
public class DrugController {

    @Autowired
    private DrugService drugService;

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/add-drug")
    public ResponseEntity<String> addDrug(@RequestBody AddDrugRequest request) {
        String response = drugService.createDrug(request);
        return ResponseEntity.ok(response);
    }




}
