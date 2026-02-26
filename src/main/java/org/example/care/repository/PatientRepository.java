package org.example.care.repository;

import java.util.List;
import java.util.Optional;

import org.example.care.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByNameContainingIgnoreCase(String name);

    Optional<Patient> findByName(String name);
}
