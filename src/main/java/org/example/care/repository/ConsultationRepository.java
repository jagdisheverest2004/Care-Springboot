package org.example.care.repository;

import org.example.care.model.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation,Long> {

    @Query("SELECT c FROM Consultation c WHERE c.doctor.user.id = :doctorUserId")
    List<Consultation> findVisitedPatientsByDoctorUserId(Long doctorUserId);
}
