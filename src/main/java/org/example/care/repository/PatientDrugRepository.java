package org.example.care.repository;

import org.example.care.model.PatientDrug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientDrugRepository extends JpaRepository<PatientDrug,Long> {
}
