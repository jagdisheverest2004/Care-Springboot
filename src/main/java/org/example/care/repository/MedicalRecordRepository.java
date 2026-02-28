package org.example.care.repository;

import java.util.List;
import java.util.Optional;
import org.example.care.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
}
