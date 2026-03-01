package org.example.care.repository;

import org.example.care.model.PatientDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientDoctorRepository extends JpaRepository<PatientDoctor,Long> {

    @Query("SELECT pd FROM PatientDoctor pd WHERE pd.doctor.id = :doctorUserId")
    List<PatientDoctor> findVisitedPatientsByDoctorUserId(Long doctorUserId);
}
