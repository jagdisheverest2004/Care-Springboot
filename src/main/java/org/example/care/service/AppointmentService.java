package org.example.care.service;

import org.example.care.dto.appointment.CreateAppointmentRequest;
import org.example.care.dto.appointment.GetAppointmentResponse;
import org.example.care.model.Appointment;
import org.example.care.model.Doctor;
import org.example.care.model.Patient;
import org.example.care.model.enumeration.AppointmentStatus;
import org.example.care.repository.AppointmentRepository;
import org.example.care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Runs automatically every hour to flag missed appointments
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markNoShows() {
        List<Appointment> missedAppointments = appointmentRepository.findByStatusAndAppointmentDateBefore(
                AppointmentStatus.SCHEDULED, LocalDateTime.now()
        );

        for (Appointment app : missedAppointments) {
            app.setStatus(AppointmentStatus.NO_SHOW);
        }
        appointmentRepository.saveAll(missedAppointments);
    }

    public List<GetAppointmentResponse> getAppointmentsForDoctor(List<Appointment> appointments) {
        return appointments.stream().map(app -> {
            GetAppointmentResponse response = new GetAppointmentResponse();
            response.setAppointmentId(app.getId());
            response.setPatientId(app.getPatient().getId());
            response.setPatientName(app.getPatient().getName());
            response.setAppointmentDateTime(app.getAppointmentDate());
            response.setAppointmentStatus(app.getStatus());
            response.setReasonForAppointment(app.getReasonForAppointment());
            return response;
        }).toList();
    }

    public void schedulePatientAppointment(Doctor doctor, CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + request.getPatientId()));

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentDate(request.getAppointmentDateTime());
        appointment.setReasonForAppointment(request.getReasonForAppointment());
        appointmentRepository.save(appointment);
    }
}