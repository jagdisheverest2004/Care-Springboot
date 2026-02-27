package org.example.care.repository;

import java.util.List;
import java.util.Optional;
import org.example.care.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    @Query("SELECT m FROM MedicalRecord m WHERE m.id= :id and m.patient.id = :patientId")
    Optional<MedicalRecord> findByIdAndPatientId(Long id, Long patientId);
}
