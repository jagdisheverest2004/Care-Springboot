package org.example.care.repository;

import org.example.care.model.Appointment;
import org.example.care.model.enumeration.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    @Query("SELECT a FROM Appointment a WHERE a.status = :appointmentStatus AND a.appointmentDate < :now")
    List<Appointment> findByStatusAndAppointmentDateBefore(AppointmentStatus appointmentStatus, LocalDateTime now);
}
