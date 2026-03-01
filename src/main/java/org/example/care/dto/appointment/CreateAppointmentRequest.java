package org.example.care.dto.appointment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {
    private Long patientId;
    private LocalDateTime appointmentDateTime;
    private String reasonForAppointment;
}
