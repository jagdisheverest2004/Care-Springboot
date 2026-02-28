package org.example.care.repository;

import org.example.care.model.PatientDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientDoctorRepository extends JpaRepository<PatientDoctor,Long> {
}
