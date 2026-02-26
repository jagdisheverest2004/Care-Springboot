package org.example.care.repository;

import java.util.List;
import java.util.Optional;
import org.example.care.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientId(Long patientId);

    Optional<MedicalRecord> findByIdAndPatientId(Long id, Long patientId);
}
